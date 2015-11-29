package kz.telecom.happydrive.ui.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.CardEditActivity;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardEditParamsAdditionalFragment extends BaseFragment
        implements CardEditActivity.IsSavable, View.OnTouchListener {
    private static final String EXTRA_CARD = "extra:card";

    private EditText mEmailEditText;
    private EditText mWebsiteEditText;
    private EditText mAddressEditText;
    private EditText mAboutEditText;
    private View mMicImageButton;
    private View mMicPlayImageButton;

    private Card mCard;
    private File mAudioFile;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;


    public static BaseFragment newInstance(Card card, File audioFile) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);
        CardEditParamsAdditionalFragment fragment = new CardEditParamsAdditionalFragment();
        fragment.setArguments(bundle);
        fragment.mAudioFile = audioFile;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_edit_params_additional, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCard = getArguments().getParcelable(EXTRA_CARD);

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_email);
        mWebsiteEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_website);
        mAddressEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_address);
        mAboutEditText = (EditText) view.findViewById(R.id.fragment_card_edit_additional_et_about);
        mMicImageButton = view.findViewById(R.id.fragment_card_edit_additional_img_btn_mic);
        mMicPlayImageButton = view.findViewById(R.id.fragment_card_edit_additional_img_btn_mic_play);

        mEmailEditText.setText(mCard.getEmail());
        mAddressEditText.setText(mCard.getAddress());
        mAboutEditText.setText(mCard.getShortDesc());

        mMicImageButton.setOnTouchListener(this);
        mMicPlayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback(!mMicPlayImageButton.isSelected());
            }
        });
    }

    @Override
    public void onPause() {
        stopRecording();
        togglePlayback(false);
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecording();
                handled = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopRecording();
                handled = true;
                break;
        }

        return handled;
    }

    private void startRecording() {
        togglePlayback(false);

        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            mRecorder.prepare();
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING, 64);
            mMicImageButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecorder.start();
                }
            }, 120l);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Произошла ошибка во время записи",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 100);
            } catch (Exception ignored) {
            }

            mRecorder.release();
            mRecorder = null;
        }
    }

    private void togglePlayback(boolean play) {
        if (play) {
            try {
                mPlayer = new MediaPlayer();
                if (mAudioFile != null && mAudioFile.length() > 0) {
                    mPlayer.setDataSource(mAudioFile.getAbsolutePath());
                } else {
                    mPlayer.setDataSource(getContext(), Uri.parse(mCard.getAudio()),
                            Collections.singletonMap("Auth-Token", User.currentUser().token));
                }

                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        togglePlayback(false);
                    }
                });

                mPlayer.prepare();
                mPlayer.start();
                mMicPlayImageButton.setSelected(true);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Произошла ошибка во время прослушивания записи",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (mPlayer != null) {
            mMicPlayImageButton.setSelected(false);
            try {
                mPlayer.stop();
            } catch (Exception ignored) {
            }

            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public boolean readyForSave() {
        if (getView() == null) {
            return false;
        }

        mCard.setEmail(mEmailEditText.getText().toString());
        mCard.setAddress(mAddressEditText.getText().toString());
        mCard.setShortDesc(mAboutEditText.getText().toString());

        return true;
    }

    @Override
    public boolean onBackPressed() {
        readyForSave();
        return super.onBackPressed();
    }
}
