package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import io.fabric.sdk.android.services.common.CommonUtils;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.CardEditActivity;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.ui.PortfolioActivity;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardDetailsFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";

    private static final int INTENT_CODE_PHOTO_CAMERA = 40001;
    private static final int INTENT_CODE_PHOTO_GALLERY = 40002;
    private static final int INTENT_CODE_BACKGROUND_CAMERA = 40001;
    private static final int INTENT_CODE_BACKGROUND_GALLERY = 40002;

    private View stubView;
    private Card mCard;
    private boolean isCardUpdating = false;

    private File mTempFile;

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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_details, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        DataManager.getInstance().bus.register(this);
        MainActivity activity = (MainActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayHomeAsUpEnabled(false);
        activity.getDrawerToggle().setDrawerIndicatorEnabled(true);
        activity.getDrawerToggle().syncState();

        Bundle args = getArguments();
        if (args != null) {
            mCard = args.getParcelable(EXTRA_CARD);
        }

        final User user = User.currentUser();
        if (!isCardUpdating && mCard != null && user != null) {
            new Thread() {
                @Override
                public void run() {
                    isCardUpdating = true;

                    try {
                        if (user.card.compareTo(mCard) == 0) {
                            if (user.updateCard()) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(user.card));
                                        }
                                    });
                                }
                            }
                        } else {
                            mCard = ApiClient.getCard(mCard.id);
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataManager.getInstance().bus.post(new Card.OnCardUpdatedEvent(mCard));
                                    }
                                });
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    isCardUpdating = false;
                }
            }.start();
        }

        updateView(view, mCard);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        User user = User.currentUser();
        if (mCard != null && user != null) {
            inflater.inflate(mCard.compareTo(user.card) == 0 ?
                    !Utils.isEmpty(mCard.getFirstName()) ?
                            R.menu.fragment_card_details_full : R.menu.fragment_card_details :
                    R.menu.fragment_card_details_other, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            startActivity(new Intent(getContext(), CardEditActivity.class));
        } else if (itemId == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            Uri uri = Uri.parse("file:///file");
            shareIntent.setType("text/plain");
            String bodyString = mCard.getFirstName();

            String lastName = mCard.getLastName();
            if (!Utils.isEmpty(lastName)) {
                if (!Utils.isEmpty(bodyString)) {
                    bodyString += " " + lastName;
                } else {
                    bodyString = lastName;
                }
            }

            if (!Utils.isEmpty(mCard.getPhone())) {
                bodyString += ", " + mCard.getPhone();
            }

            if (!Utils.isEmpty(mCard.getEmail())) {
                bodyString += ", " + mCard.getEmail();
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, bodyString);
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(shareIntent, "Поделиться..."));
        } else if (itemId == R.id.action_change_photo || itemId == R.id.action_change_background) {
            new AlertDialog.Builder(getContext())
                    .setItems(new String[]{"Снять фото", "Из Галлереи"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (which == 0) {
                                        try {
                                            mTempFile = Utils.tempFileWithNow(getContext());
                                            Utils.openCamera(CardDetailsFragment.this, mTempFile,
                                                    itemId == R.id.action_change_photo ?
                                                            INTENT_CODE_PHOTO_CAMERA : INTENT_CODE_BACKGROUND_CAMERA);
                                        } catch (Exception ignored) {
                                            Logger.e("TEST", "couldn't create a file", ignored);
                                        }
                                    } else if (which == 1) {
                                        Utils.openGallery(CardDetailsFragment.this, "", "image/*",
                                                itemId == R.id.action_change_background ?
                                                        INTENT_CODE_PHOTO_GALLERY : INTENT_CODE_BACKGROUND_GALLERY);
                                    }
                                }
                            }).show();
        }

        return false;
    }

    public void updateView(View view, final Card card) {
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fragment_card_details_progress_bar);
        progressBar.setVisibility(View.GONE);

        if (card == null || card.getFirstName() == null) {
            TextView textView = (TextView) view.findViewById(R.id.stub_error_tv_msg);
            if (textView == null) {
                stubView = ((ViewStub) view.findViewById(R.id.stub_error)).inflate();
            }

            Button actionButton = (Button) view.findViewById(R.id.stub_error_btn_action);
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), CardEditActivity.class));
                }
            });

            view.findViewById(R.id.about).setVisibility(View.GONE);
            view.findViewById(R.id.about_block).setVisibility(View.GONE);
            view.findViewById(R.id.about_divider).setVisibility(View.GONE);

            ImageView background = (ImageView) view.findViewById(R.id.fragment_card_details_v_header);
            background.setImageDrawable(null);

            ImageView userPhoto = (ImageView) view.findViewById(R.id.user_photo);
            userPhoto.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.user_photo));
        } else {
            if (stubView != null) {
                ((ViewGroup) stubView.getParent()).removeView(stubView);
                stubView = null;
            }

            TextView userName = (TextView) view.findViewById(R.id.username);
            if (userName == null) {
                ((ViewStub) view.findViewById(R.id.fragment_card_details_stub)).inflate();
            }

            String userText = card.getFirstName();
            if (!TextUtils.isEmpty(card.getLastName())) {
                userText += " " + card.getLastName();
            }

            userName = (TextView) view.findViewById(R.id.username);
            userName.setText(userText);

            TextView position = (TextView) view.findViewById(R.id.position);
            position.setText(card.getPosition());

            TextView companyName = (TextView) view.findViewById(R.id.company_name);
            if (!TextUtils.isEmpty(card.getWorkPlace())) {
                companyName.setVisibility(View.VISIBLE);
                companyName.setText(card.getWorkPlace());
            } else {
                companyName.setVisibility(View.GONE);
            }

            TextView phoneNumber = (TextView) view.findViewById(R.id.phone);
            phoneNumber.setText(card.getPhone());

            view.findViewById(R.id.phone_block).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + card.getPhone())));
                }
            });

            View emailBlock = view.findViewById(R.id.email_block);
            if (!TextUtils.isEmpty(card.getEmail())) {
                TextView email = (TextView) view.findViewById(R.id.email);
                email.setText(card.getEmail());
                emailBlock.setVisibility(View.VISIBLE);
            } else {
                emailBlock.setVisibility(View.GONE);
            }

            emailBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{card.getEmail()});
                    try {
                        startActivity(Intent.createChooser(i, "Написать"));
                    } catch (ActivityNotFoundException ex) {
                    }
                }
            });

            View addressBlock = view.findViewById(R.id.address_block);
            if (!TextUtils.isEmpty(card.getAddress())) {
                TextView address = (TextView) view.findViewById(R.id.address);
                address.setText(card.getAddress());
                addressBlock.setVisibility(View.VISIBLE);
            } else {
                addressBlock.setVisibility(View.GONE);
            }

            addressBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String uri = "geo:0,0?q=" + card.getAddress();
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                    }
                }
            });

            TextView aboutTextView = (TextView) view.findViewById(R.id.about);
            if (!TextUtils.isEmpty(card.getShortDesc())) {
                aboutTextView.setText(card.getShortDesc());
                aboutTextView.setVisibility(View.VISIBLE);
            } else {
                aboutTextView.setVisibility(View.GONE);
            }

            view.findViewById(R.id.portfolio_text).setVisibility(View.VISIBLE);
            view.findViewById(R.id.portfolio_block).setVisibility(View.VISIBLE);
            view.findViewById(R.id.about_block).setVisibility(View.VISIBLE);
            view.findViewById(R.id.about_divider).setVisibility(View.VISIBLE);

            view.findViewById(R.id.foto_block).setOnClickListener(this);
            view.findViewById(R.id.video_block).setOnClickListener(this);

            final ImageView background = (ImageView) view.findViewById(R.id.fragment_card_details_v_header);
            if (!Utils.isEmpty(card.getBackground())) {
                background.post(new Runnable() {
                    @Override
                    public void run() {
                        NetworkManager.getPicasso()
                                .load(card.getBackground())
                                .config(Bitmap.Config.RGB_565)
                                .resize(background.getWidth(), background.getHeight())
                                .onlyScaleDown()
                                .centerCrop()
                                .into(background);
                    }
                });
            } else {
                background.setImageDrawable(null);
            }

            final Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.user_photo);
            final ImageView userPhoto = (ImageView) view.findViewById(R.id.user_photo);
            if (!Utils.isEmpty(card.getAvatar())) {
                userPhoto.post(new Runnable() {
                    @Override
                    public void run() {
                        NetworkManager.getPicasso()
                                .load(card.getAvatar())
                                .config(Bitmap.Config.RGB_565)
                                .resize(userPhoto.getWidth(), userPhoto.getHeight())
                                .placeholder(drawable)
                                .error(drawable)
                                .centerCrop()
                                .into(userPhoto);
                    }
                });
            } else {
                userPhoto.setImageDrawable(drawable);
            }
        }

        if (card == null || Utils.isEmpty(card.getFirstName())
                || User.currentUser() == null || card.id != User.currentUser().card.id) {
            view.findViewById(R.id.portfolio_text).setVisibility(View.GONE);
            view.findViewById(R.id.portfolio_block).setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.foto_block) {
//            intent = new Intent(getContext(), PortfolioActivity.class);
//            intent.putExtra(PortfolioActivity.EXTRA_TYPE, PortfolioActivity.EXTRA_TYPE_PHOTO);
        } else if (v.getId() == R.id.video_block) {
//            intent = new Intent(getContext(), PortfolioActivity.class);
//            intent.putExtra(PortfolioActivity.EXTRA_TYPE, PortfolioActivity.EXTRA_TYPE_VIDEO);
        }

        if (intent != null) {
            startActivity(intent);
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
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case INTENT_CODE_PHOTO_CAMERA:
//                Uri u;
//                if (hasImageCaptureBug()) {
//                    File fi = new File("/sdcard/tmp");
//                    try {
//                        u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContext()
//                                .getContentResolver(), fi.getAbsolutePath(), null, null));
//                        if (!fi.delete()) {
//                            Log.i("logMarker", "Failed to delete " + fi);
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    u = data.getData();
//                }
//        }
        if (requestCode == INTENT_CODE_PHOTO_CAMERA) {
            if (mTempFile != null) {
                if (mTempFile.exists()) {
                    Logger.i("TEST", "the file exists");

                    Logger.i("TEST", "fileSize: " + Integer.parseInt(String.valueOf(mTempFile.length() / 1024)));
                }

                Drawable drawable = Drawable.createFromPath(mTempFile.getAbsolutePath());
                View view = getView();
                if (view != null) {
                    final ImageView userPhoto = (ImageView) view.findViewById(R.id.user_photo);
                    userPhoto.setImageDrawable(drawable);
                }
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_CODE_PHOTO_GALLERY ||
                    requestCode == INTENT_CODE_BACKGROUND_GALLERY) {
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
                                isSuccessful = User.currentUser().changeAvatar(file);
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
                                            Uri uriFromPath = Uri.fromFile(file);

                                            View view = getView();
                                            if (view != null) {
                                                final ImageView userPhoto = (ImageView) view.findViewById(R.id.user_photo);
                                                userPhoto.setImageURI(uriFromPath);
                                            }
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

    public boolean hasImageCaptureBug() {
        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);
    }
}
