package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.File;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";
    private static final int INTENT_CODE_PHOTO_CAMERA = 10001;
    private static final int INTENT_CODE_PHOTO_GALLERY = 10002;
    private static final int INTENT_CODE_BACKGROUND_CAMERA = 10003;
    private static final int INTENT_CODE_BACKGROUND_GALLERY = 10004;

    private File mTempFile;

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);

        BaseFragment fragment = new MainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.app_name);

        TextView usernameTextView = (TextView) view.findViewById(R.id.user_name);
        TextView positionTextView = (TextView) view.findViewById(R.id.user_position);

        final Card card = getArguments().getParcelable(EXTRA_CARD);
        String lastName = card.getLastName();
        String username = card.getFirstName();
        if (!Utils.isEmpty(lastName)) {
            if (!Utils.isEmpty(username)) {
                username += " " + lastName;
            } else {
                username = lastName;
            }
        }

        usernameTextView.setText(username);
        positionTextView.setText(card.getPosition());

        view.findViewById(R.id.photo_image_view).setOnClickListener(this);
        view.findViewById(R.id.video_image_view).setOnClickListener(this);
        view.findViewById(R.id.music_image_view).setOnClickListener(this);
        view.findViewById(R.id.document_image_view).setOnClickListener(this);

        final ImageView userPhotoImageView = (ImageView) view.findViewById(R.id.user_photo);
        userPhotoImageView.setOnClickListener(this);
        if (card.getAvatar() != null) {
            userPhotoImageView.post(new Runnable() {
                @Override
                public void run() {
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.user_photo);
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    NetworkManager.getGlide()
                            .load(card.getAvatar())
                            .signature(GlideCacheSignature
                                    .ownerAvatarKey(card.getAvatar()))
                            .placeholder(drawable)
                            .error(drawable)
                            .bitmapTransform(new CenterCrop(getContext()),
                                    new GlideRoundedCornersTransformation(getContext(),
                                            Utils.dipToPixels(6f, dm), Utils.dipToPixels(2f, dm)))
                            .override(userPhotoImageView.getWidth(),
                                    userPhotoImageView.getHeight())
                            .into(userPhotoImageView);
                }
            });
        }

        final ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background);
        backgroundImageView.setOnClickListener(this);
        if (card.getBackground() != null) {
            backgroundImageView.post(new Runnable() {
                @Override
                public void run() {
                    NetworkManager.getGlide()
                            .load(card.getBackground())
                            .signature(GlideCacheSignature
                                    .ownerBackgroundKey(card.getBackground()))
                            .override(backgroundImageView.getWidth(),
                                    backgroundImageView.getHeight())
                            .centerCrop()
                            .into(backgroundImageView);

                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.user_photo || viewId == R.id.background) {
            new AlertDialog.Builder(getContext())
                    .setItems(new String[]{"Снять фото", "Из Галлереи"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (which == 0) {
                                        try {
                                            mTempFile = Utils.tempFileWithNow(getContext());
                                            Utils.openCamera(MainFragment.this, mTempFile,
                                                    viewId == R.id.user_photo ?
                                                            INTENT_CODE_PHOTO_CAMERA :
                                                            INTENT_CODE_BACKGROUND_CAMERA);
                                        } catch (Exception ignored) {
                                            Logger.e("TEST", "couldn't create a file", ignored);
                                        }
                                    } else if (which == 1) {
                                        Utils.openGallery(MainFragment.this, "", "image/*",
                                                viewId == R.id.user_photo ?
                                                        INTENT_CODE_PHOTO_GALLERY :
                                                        INTENT_CODE_BACKGROUND_GALLERY);
                                    }
                                }
                            }).show();

            return;
        }

        Intent intent = null;
        if (view.getId() == R.id.photo_image_view) {
//            intent = new Intent(getContext(), StorageActivity.class);
//            intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.EXTRA_TYPE_PHOTO);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_CODE_PHOTO_CAMERA || requestCode == INTENT_CODE_BACKGROUND_CAMERA) {
                if (mTempFile != null && mTempFile.length() > 0) {
                    final File file = mTempFile;
                    mTempFile = null;

                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setMessage("Сохранение...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            boolean isSuccessful = false;
                            try {
                                isSuccessful = requestCode == INTENT_CODE_PHOTO_CAMERA ?
                                        User.currentUser().changeAvatar(file) :
                                        User.currentUser().changeBackground(file);
                            } catch (Exception e) {
                                Logger.e("TEST", e.getLocalizedMessage(), e);
                            }

                            final boolean success = isSuccessful;
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (success) {
                                            if (requestCode == INTENT_CODE_PHOTO_CAMERA) {
                                                GlideCacheSignature.invalidateAvatarKey();
                                            } else {
                                                GlideCacheSignature.invalidateBackgroundKey();
                                            }

                                            DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(
                                                    User.currentUser().card));
                                        }
                                    }
                                });
                            }
                        }
                    }.start();
                }
            } else if (requestCode == INTENT_CODE_PHOTO_GALLERY || requestCode == INTENT_CODE_BACKGROUND_GALLERY) {
                try {
                    String path = null;
                    if (Build.VERSION.SDK_INT < 11) {
                        path = Utils.getRealPathFromURI_BelowAPI11(getContext(), data.getData());
                    } else if (Build.VERSION.SDK_INT < 19) {
                        path = Utils.getRealPathFromURI_API11to18(getContext(), data.getData());
                    } else {
                        path = Utils.getRealPathFromURI_API19(getContext(), data.getData());
                    }

                    final File file = new File(path);
                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setMessage("Сохранение...");
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            boolean isSuccessful = false;
                            try {
                                isSuccessful = requestCode == INTENT_CODE_PHOTO_GALLERY ?
                                        User.currentUser().changeAvatar(file) :
                                        User.currentUser().changeBackground(file);
                            } catch (Exception e) {
                                Logger.e("TEST", e.getLocalizedMessage(), e);
                            }

                            final boolean success = isSuccessful;
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (success) {
                                            if (requestCode == INTENT_CODE_PHOTO_GALLERY) {
                                                GlideCacheSignature.invalidateAvatarKey();
                                            } else {
                                                GlideCacheSignature.invalidateBackgroundKey();
                                            }

                                            final Card card = User.currentUser().card;

                                            View view = getView();
                                            if (view != null) {
                                                final ImageView backgroundImageView = (ImageView) view.findViewById(R.id.background);
                                                if (backgroundImageView != null) {
                                                    backgroundImageView.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            NetworkManager.getGlide()
                                                                    .load(card.getBackground())
                                                                    .signature(GlideCacheSignature
                                                                            .ownerBackgroundKey(card.getBackground()))
                                                                    .override(backgroundImageView.getWidth(),
                                                                            backgroundImageView.getHeight())
                                                                    .centerCrop()
                                                                    .into(backgroundImageView);
                                                        }
                                                    });
                                                }
                                            }

                                            DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(card));
                                        }
                                    }
                                });
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    Logger.e("TEST", e.getLocalizedMessage(), e);
                }
            }
        }
    }
}