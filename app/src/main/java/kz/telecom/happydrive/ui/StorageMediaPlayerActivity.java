package kz.telecom.happydrive.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.util.Util;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.FileObject;
import kz.telecom.happydrive.ui.widget.ExoMediaPlayer;
import kz.telecom.happydrive.ui.widget.ExtractorRendererBuilder;
import kz.telecom.happydrive.util.Logger;

/**
 * Created by shgalym on 25.12.2015.
 */
public class StorageMediaPlayerActivity extends BaseActivity implements SurfaceHolder.Callback,
        AudioCapabilitiesReceiver.Listener, ExoMediaPlayer.Listener {
    private static final String TAG = Logger.makeLogTag("StorageMediaPlayerActivity");
    public static final String EXTRA_FILE = "extra:file";

    private AspectRatioFrameLayout mVideoFrameLayout;
    private SurfaceView mSurfaceView;
    private ImageView mShutterImageView;

    private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;
    private MediaController mediaController;
    private ExoMediaPlayer mPlayer;

    private FileObject mFileObject;
    private long mPlayerPosition;
    private boolean mPlayerNeedsPrepare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_media_player);

        mFileObject = getIntent().getParcelableExtra(EXTRA_FILE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        ActionBar actionBar = initToolbar(toolbar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mFileObject.name);
        actionBar.hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        final View rootView = findViewById(R.id.activity_storage_media_player_v_root);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlVisibility();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        mVideoFrameLayout = (AspectRatioFrameLayout) findViewById(R.id.activity_storage_media_player_fl_video_frame);
        mSurfaceView = (SurfaceView) findViewById(R.id.activity_storage_media_player_surface_view);
        mSurfaceView.getHolder().addCallback(this);

        mShutterImageView = (ImageView) findViewById(R.id.activity_storage_media_player_img_view_shutter);
        if (mFileObject.getType() == ApiObject.TYPE_FILE_MUSIC) {
            mShutterImageView.setImageResource(R.drawable.ic_cloud_music);
            mShutterImageView.setColorFilter(ColorUtils.setAlphaComponent(
                    ContextCompat.getColor(this, R.color.colorAccent), 160));
        }

        mediaController = new KeyCompatibleMediaController(this);
        mediaController.setAnchorView(rootView);

        mAudioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
        mAudioCapabilitiesReceiver.register();
    }


    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        mPlayerPosition = 0;
        setIntent(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mPlayer == null) {
            preparePlayer(true);
        } else {
            mPlayer.setBackgrounded(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
        mShutterImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_LONG).show();
        Logger.e(TAG, e.getLocalizedMessage(), e);
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        mShutterImageView.setVisibility(View.GONE);
        mVideoFrameLayout.setAspectRatio(height == 0 ? 1 :
                (width * pixelWidthAspectRatio) / height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.blockingClearSurface();
        }
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (mPlayer == null) {
            return;
        }
        boolean backgrounded = mPlayer.getBackgrounded();
        boolean playWhenReady = mPlayer.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        mPlayer.setBackgrounded(backgrounded);
    }

    private void preparePlayer(boolean playWhenReady) {
        if (mPlayer == null) {
            String userAgent = Util.getUserAgent(this, "StorageMediaPlayer");
            mPlayer = new ExoMediaPlayer(new ExtractorRendererBuilder(this,
                    userAgent, Uri.parse(mFileObject.url)));
            mPlayer.addListener(this);
            mPlayer.seekTo(mPlayerPosition);
            mPlayerNeedsPrepare = true;
            mediaController.setMediaPlayer(mPlayer.getPlayerControl());
            mediaController.setEnabled(true);
        }

        if (mPlayerNeedsPrepare) {
            mPlayer.prepare();
            mPlayerNeedsPrepare = false;
        }

        mPlayer.setSurface(mSurfaceView.getHolder().getSurface());
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayerPosition = mPlayer.getCurrentPosition();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void toggleControlVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
            getSupportActionBar().hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
        getSupportActionBar().show();
    }

    private static final class KeyCompatibleMediaController extends MediaController {
        private MediaController.MediaPlayerControl mPlayerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.mPlayerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (mPlayerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mPlayerControl.seekTo(mPlayerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }

                return true;
            } else if (mPlayerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mPlayerControl.seekTo(mPlayerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }

                return true;
            }

            return super.dispatchKeyEvent(event);
        }
    }
}
