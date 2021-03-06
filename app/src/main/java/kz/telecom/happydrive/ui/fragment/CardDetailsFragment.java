package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.Collections;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiObject;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.FolderObject;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.CardEditActivity;
import kz.telecom.happydrive.ui.CatalogItemActivity;
import kz.telecom.happydrive.ui.StorageActivity;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardDetailsFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";
    private static final String TAG = Logger.makeLogTag("CardDetailsFragment");
    private static final int INTENT_CODE_PHOTO = 10001;

    private ProgressDialog mProgressDialog;

    private Card mCard;
    private boolean isCardUpdating = false;

    private MediaPlayer mPlayer = null;

    public static BaseFragment newInstance(Card card) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_CARD, card);

        BaseFragment fragment = new CardDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        DataManager.getInstance().bus.register(this);
        BaseActivity activity = (BaseActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionBar.setTitle("");

        mCard = getArguments().getParcelable(EXTRA_CARD);

        final User user = User.currentUser();
        if (!isCardUpdating && mCard != null && user != null) {
            new Thread() {
                @Override
                public void run() {
                    isCardUpdating = true;

                    try {
                        if (user.card.compareTo(mCard) == 0) {
                            if (user.updateCard()) {
                                mCard = user.card;
                            }
                        } else {
                            mCard = ApiClient.getCard(mCard.id);
                        }

                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(mCard));
                                }
                            });
                        }
                    } catch (final Exception e) {
                        final Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (e instanceof ApiResponseError && ((ApiResponseError) e)
                                            .apiErrorCode == ApiResponseError.API_RESPONSE_CODE_CARD_INVISIBLE) {
                                        Toast.makeText(activity, "Визитка закрыта из общего доступа",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    isCardUpdating = false;
                }
            }.start();
        }

        updateView(view, mCard);
    }

    public void updateView(View view, final Card card) {
        final Button createButton = (Button) view.findViewById(R.id.fragment_card_btn_create);
        final ImageButton photoButton = (ImageButton) view.findViewById(R.id.fragment_card_img_button_photo);
        final ImageButton shareButton = (ImageButton) view.findViewById(R.id.fragment_card_img_button_share);
        final View aboutContainer = view.findViewById(R.id.fragment_card_rl_about_container);
        final View portfolioContainer = view.findViewById(R.id.fragment_card_rl_portfolio_container);
        if (card == null || card.getCategoryId() <= 0) {
            aboutContainer.setVisibility(View.GONE);
            portfolioContainer.setVisibility(View.GONE);
            photoButton.setVisibility(View.GONE);
            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), CardEditActivity.class));
                }
            });
        } else {
            createButton.setVisibility(View.GONE);

            if (card.publicFolders.size() > 0) {
                portfolioContainer.setVisibility(View.VISIBLE);
                portfolioContainer.findViewById(R.id.fragment_card_tv_portfolio_photo)
                        .setOnClickListener(this);
                portfolioContainer.findViewById(R.id.fragment_card_tv_portfolio_video)
                        .setOnClickListener(this);
            } else {
                portfolioContainer.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(card.getShortDesc())
                    || !TextUtils.isEmpty(card.getFullDesc())) {
                TextView titleTextView = (TextView) aboutContainer.findViewById(R.id.fragment_card_tv_about_title);
                final TextView textTextView = (TextView) aboutContainer.findViewById(R.id.fragment_card_tv_about_text);
                textTextView.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(card.getFullDesc())) {
                    titleTextView.setText("О компании");
                    textTextView.setText(card.getFullDesc());
                } else {
                    titleTextView.setText("О себе");
                    textTextView.setText(card.getShortDesc());
                }

                aboutContainer.setVisibility(View.VISIBLE);
                final View expandTogglerImgBtn = aboutContainer.findViewById(R.id.fragment_card_img_btn_about_expander);
                aboutContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (textTextView.getVisibility() != View.GONE) {
                            textTextView.setVisibility(View.GONE);
                            expandTogglerImgBtn.setSelected(false);
                        } else {
                            textTextView.setVisibility(View.VISIBLE);
                            expandTogglerImgBtn.setSelected(true);
                        }
                    }
                });
            } else {
                aboutContainer.setVisibility(View.GONE);
            }

            if (card.compareTo(User.currentUser().card) == 0) {
                photoButton.setVisibility(View.VISIBLE);
                photoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getContext())
                                .setItems(new String[]{"Снять фото", "Из Галлереи"},
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                try {
                                                    if (which == 0) {
                                                        EasyImage.openCamera(CardDetailsFragment.this);
                                                    } else if (which == 1) {
                                                        EasyImage.openGallery(CardDetailsFragment.this);
                                                    }
                                                } catch (Exception e) {
                                                    Logger.e(TAG, e.getLocalizedMessage(), e);
                                                    Toast.makeText(getContext(), "Произошла ошибка во время запуска Intent'а." +
                                                            " Пожалуйста, сообщите в службу поддержки.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).show();
                    }
                });
            } else {
                photoButton.setVisibility(View.GONE);
            }

            TextView userNameTextView = (TextView) view.findViewById(R.id.fragment_card_tv_name);

            String userText = card.getFirstName();
            if (!TextUtils.isEmpty(card.getLastName())) {
                userText += "\n" + card.getLastName();
            }

            userNameTextView.setText(userText);

            TextView positionTextView = (TextView) view.findViewById(R.id.fragment_card_tv_position);
            if (!TextUtils.isEmpty(card.getPosition())) {
                positionTextView.setVisibility(View.VISIBLE);
                positionTextView.setText(card.getPosition());
            } else {
                positionTextView.setVisibility(View.GONE);
            }

            TextView workplaceTextView = (TextView) view.findViewById(R.id.fragment_card_tv_work_place);
            if (!TextUtils.isEmpty(card.getWorkPlace())) {
                workplaceTextView.setVisibility(View.VISIBLE);
                workplaceTextView.setText(card.getWorkPlace());
            } else {
                workplaceTextView.setVisibility(View.GONE);
            }

            View phoneContainer = view.findViewById(R.id.fragment_card_ll_phone_container);
            if (!TextUtils.isEmpty(card.getPhone())) {
                phoneContainer.setVisibility(View.VISIBLE);
                TextView textView = (TextView) phoneContainer.findViewById(R.id.fragment_card_tv_phone);
                ImageView imageView = (ImageView) phoneContainer.findViewById(R.id.fragment_card_img_view_phone);
                imageView.setColorFilter(0xff959595);
                textView.setText(card.getPhone());

                phoneContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + card.getPhone())));
                    }
                });
            } else {
                phoneContainer.setVisibility(View.GONE);
            }

            View emailContainer = view.findViewById(R.id.fragment_card_ll_email_container);
            if (!TextUtils.isEmpty(card.getEmail())) {
                emailContainer.setVisibility(View.VISIBLE);
                TextView emailTextView = (TextView) emailContainer.findViewById(R.id.fragment_card_tv_email);
                ImageView imageView = (ImageView) emailContainer.findViewById(R.id.fragment_card_img_view_email);
                imageView.setColorFilter(0xff959595);
                emailTextView.setText(card.getEmail());

                emailContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", card.getEmail(), null));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{card.getEmail()});
                            startActivity(Intent.createChooser(intent, "Написать письмо..."));
                        } catch (ActivityNotFoundException ignored) {
                        }
                    }
                });
            } else {
                emailContainer.setVisibility(View.GONE);
            }

            View addressContainer = view.findViewById(R.id.fragment_card_ll_address_container);
            if (!TextUtils.isEmpty(card.getAddress())) {
                addressContainer.setVisibility(View.VISIBLE);
                TextView addressTextView = (TextView) addressContainer.findViewById(R.id.fragment_card_tv_address);
                ImageView imageView = (ImageView) addressContainer.findViewById(R.id.fragment_card_img_view_address);
                imageView.setColorFilter(0xff959595);
                addressTextView.setText(card.getAddress());

                addressContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String uri = "geo:0,0?q=" + card.getAddress();
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(Intent.createChooser(intent, "Геолокация..."));
                        } catch (ActivityNotFoundException ignored) {
                        }
                    }
                });
            } else {
                addressContainer.setVisibility(View.GONE);
            }
        }

        final ImageButton audioImgBtn = (ImageButton) view.findViewById(R.id.fragment_card_img_button_audio);
        if (!Utils.isEmpty(card.getAudio())) {
            audioImgBtn.setVisibility(View.VISIBLE);
            audioImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleAudioPlayback(!(mPlayer != null && mPlayer.isPlaying()));
                }
            });
        } else {
            audioImgBtn.setVisibility(View.GONE);
        }

        final ImageView userPhoto = (ImageView) view.findViewById(R.id.fragment_card_img_view_avatar);
        if (!Utils.isEmpty(card.getAvatar())) {
            NetworkManager.getGlide()
                    .load(card.getAvatar())
                    .signature(card.compareTo(User.currentUser().card) == 0 ?
                            GlideCacheSignature.ownerAvatarKey(card.getAvatar()) :
                            GlideCacheSignature.foreignCacheKey(card.getAvatar()))
                    .centerCrop()
                    .into(userPhoto);
        } else {
            userPhoto.setImageDrawable(null);
        }

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(card.getFirstName());
                        String lastName = card.getLastName();
                        if (!Utils.isEmpty(lastName)) {
                            if (stringBuilder.length() != 0) {
                                stringBuilder.append(" ");
                            }

                            stringBuilder.append(lastName);
                        }

                        if (!Utils.isEmpty(card.getWorkPlace())) {
                            stringBuilder.append(", ")
                                    .append(card.getWorkPlace());
                        }

                        if (!Utils.isEmpty(card.getPhone())) {
                            stringBuilder.append(",\n").append(card.getPhone());
                        }

                        if (!Utils.isEmpty(card.getEmail())) {
                            stringBuilder.append(",\n").append(card.getEmail());
                        }

                        if (!Utils.isEmpty(card.getDomain())) {
                            stringBuilder.append("\nhttps://")
                                    .append(card.getDomain())
                                    .append(".happy-drive.kz");
                        }

                        Uri pictureUri = Utils.getLocalBitmapUri(userPhoto);
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                        shareIntent.setType("image/*");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Поделиться..."));
                    }
                };

                if (!card.isVisible() && User.currentUser().card.compareTo(card) == 0) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Визитка скрыта")
                            .setMessage("Ваша визитка скрыта и не показывается в каталоге. Хотите показать?")
                            .setPositiveButton("Показать", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                ApiClient.setVisibility(!card.isVisible());
                                                User.currentUser().updateCard();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }.start();
                                }
                            }).setNegativeButton("Отмена", null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    runnable.run();
                                }
                            }).show();
                    return;
                }

                runnable.run();
            }
        });

        if (getActivity() instanceof CatalogItemActivity) {
            ((CatalogItemActivity) getActivity()).changeBackgroundImage(card);
        }
    }

    @Override
    public void onPause() {
        toggleAudioPlayback(false);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.fragment_card_tv_portfolio_photo) {
            for (FolderObject obj : mCard.publicFolders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_PHOTO) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, mCard);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_PHOTO);
                    break;
                }
            }
        } else if (v.getId() == R.id.fragment_card_tv_portfolio_video) {
            for (FolderObject obj : mCard.publicFolders) {
                if (obj.getType() == ApiObject.TYPE_FOLDER_VIDEO) {
                    intent = new Intent(getContext(), StorageActivity.class);
                    intent.putExtra(StorageActivity.EXTRA_FOLDER, obj);
                    intent.putExtra(StorageActivity.EXTRA_CARD, mCard);
                    intent.putExtra(StorageActivity.EXTRA_TYPE, StorageActivity.TYPE_VIDEO);
                    break;
                }
            }
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void toggleAudioPlayback(boolean play) {
        if (play) {
            try {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(getContext(), Uri.parse(mCard.getAudio()),
                        Collections.singletonMap("Auth-Token", User.currentUser().token));

                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        toggleAudioPlayback(false);
                    }
                });

                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Произошла ошибка во время прослушивания записи",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (Exception ignored) {
            }

            mPlayer.release();
            mPlayer = null;
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardUpdate(Card.OnCardUpdatedEvent event) {
        updateView(getView(), event.card);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource imageSource) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource) {
                uploadFile(file);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource imageSource) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                if (imageSource == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            }
        });
    }

    public void uploadFile(final File file) {
        if (file == null || file.length() < 0) {
            return;
        }

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("Сохранение...");
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (User.currentUser().changeAvatar(file)) {
                        GlideCacheSignature.invalidateAvatarKey();
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(mCard));
                            }
                        });
                    }
                } catch (final Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                if (e instanceof NoConnectionError) {
                                    if (getView() != null) {
                                        Snackbar.make(getView(), R.string.no_connection,
                                                Snackbar.LENGTH_LONG)
                                                .setAction("Повторить", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mProgressDialog.show();
                                                        uploadFile(file);
                                                    }
                                                }).show();
                                    } else {
                                        Toast.makeText(getContext(), R.string.no_connection,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else if (e instanceof ApiResponseError) {
                                    final int errorCode = ((ApiResponseError) e).apiErrorCode;
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Ошибка")
                                            .setMessage("Произошла ошибка во время сохранения файла. " +
                                                    "Пожалуйста, сообщите код ошибки службе поддержки: " + errorCode)
                                            .show();
                                } else {
                                    Toast.makeText(getContext(), "Произошла неизвестная ошибка. Повторите еще раз." +
                                                    " Если ошибка повторяется, пожалуйста, обратитесь в службу поддержки",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }.start();
    }
}