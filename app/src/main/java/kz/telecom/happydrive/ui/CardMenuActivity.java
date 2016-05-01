package kz.telecom.happydrive.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.util.Logger;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by shgalym on 4/9/16.
 */
public class CardMenuActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = Logger.makeLogTag("SettingsFragment");

    private ProgressDialog mProgressDialog;

    private boolean isLoading;
    private boolean isAvatar;

    private Uri mCroppedFileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        }

        setContentView(R.layout.activity_card_menu);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.activity_card_menu_fake_toolbar_back) {
            finish();
        } else if (viewId == R.id.action_edit_card) {
            startActivity(new Intent(this, CardEditActivity.class));
        } else if (viewId == R.id.action_change_background) {
            new AlertDialog.Builder(this)
                    .setTitle("Изменить фон")
                    .setItems(new String[]{"Снять фото", "Из Галереи", "Галерея фонов"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    isAvatar = false;
                                    try {
                                        if (which == 0) {
                                            EasyImage.openCamera(CardMenuActivity.this);
                                        } else if (which == 1) {
                                            EasyImage.openGallery(CardMenuActivity.this);
                                        }
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.getLocalizedMessage(), e);
                                        Toast.makeText(CardMenuActivity.this, "Произошла ошибка во время запуска Intent'а." +
                                                " Пожалуйста, сообщите в службу поддержки.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
        } else if (viewId == R.id.action_change_avatar) {
            new AlertDialog.Builder(this)
                    .setItems(new String[]{"Снять фото", "Из Галереи"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    isAvatar = true;
                                    try {
                                        if (which == 0) {
                                            EasyImage.openCamera(CardMenuActivity.this);
                                        } else if (which == 1) {
                                            EasyImage.openGallery(CardMenuActivity.this);
                                        }
                                    } catch (Exception e) {
                                        Logger.e(TAG, e.getLocalizedMessage(), e);
                                        Toast.makeText(CardMenuActivity.this, "Произошла ошибка во время запуска Intent'а." +
                                                " Пожалуйста, сообщите в службу поддержки.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
        } else if (viewId == R.id.action_change_domain) {
            startActivity(new Intent(this, ChangeDomainActivity.class));
        }
    }

    private void uploadFile(final File file) {
        if (file == null || file.length() < 0) {
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Сохранение...");
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (isAvatar) {
                        if (User.currentUser().changeAvatar(file)) {
                            GlideCacheSignature.invalidateAvatarKey();
                        }
                    } else {
                        if (User.currentUser().changeBackground(file)) {
                            GlideCacheSignature.invalidateBackgroundKey();
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (isAvatar) {
                                DataManager.getInstance().bus
                                        .post(new Card.OnCardUpdatedEvent(User.currentUser().card));
                            } else {
                                DataManager.getInstance().bus
                                        .post(new Card.OnBackgroundUpdatedEvent(User.currentUser().card));
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            if (e instanceof NoConnectionError) {
                                Snackbar.make(getWindow().getDecorView(), R.string.no_connection,
                                        Snackbar.LENGTH_LONG)
                                        .setAction("Повторить", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mProgressDialog.show();
                                                uploadFile(file);
                                            }
                                        }).show();
                            } else if (e instanceof ApiResponseError)

                            {
                                final int errorCode = ((ApiResponseError) e).apiErrorCode;
                                new AlertDialog.Builder(CardMenuActivity.this)
                                        .setTitle("Ошибка")
                                        .setMessage("Произошла ошибка во время сохранения файла. " +
                                                "Пожалуйста, сообщите код ошибки службе поддержки: " + errorCode)
                                        .show();
                            } else

                            {
                                Toast.makeText(CardMenuActivity.this, "Произошла неизвестная ошибка. Повторите еще раз." +
                                                " Если ошибка повторяется, пожалуйста, обратитесь в службу поддержки",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }

                .

                        start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            uploadFile(new File(mCroppedFileUri.getPath()));
            return;
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource) {
                if (isAvatar) {
                    mCroppedFileUri = Uri.fromFile(file);
                    Crop.of(Uri.fromFile(file), mCroppedFileUri).start(CardMenuActivity.this, 123);
                    uploadFile(file);
                } else {
                    uploadFile(file);
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource imageSource) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                if (imageSource == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(CardMenuActivity.this);
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            }
        });
    }
}
