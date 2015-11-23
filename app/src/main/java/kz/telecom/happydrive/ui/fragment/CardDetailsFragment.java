package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.CardEditActivity;
import kz.telecom.happydrive.ui.PortfolioActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class CardDetailsFragment extends BaseFragment implements View.OnClickListener {
    public static final String EXTRA_CARD = "extra:card";

    private View stubView;
    private Card mShareCard;
    private boolean isCardUpdating = false;

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

        Card card = null;
        Bundle args = getArguments();
        if (args != null) {
            card = args.getParcelable(EXTRA_CARD);
        }

        final User user = User.currentUser();
        if (!isCardUpdating && card != null && user != null) {
            if (user.card.compareTo(card) == 0) {
                new Thread() {
                    @Override
                    public void run() {
                        isCardUpdating = true;

                        try {
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
                        } catch (Exception ignored) {
                        }

                        isCardUpdating = false;
                    }
                }.start();
            }
        }

        updateView(view, card);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_card_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            startActivity(new Intent(getContext(), CardEditActivity.class));
        } else if (item.getItemId() == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            Uri uri = Uri.parse("file:///file");
            shareIntent.setType("text/plain");
            String bodyString = mShareCard.getFirstName();

            String lastName = mShareCard.getLastName();
            if (!Utils.isEmpty(lastName)) {
                if (!Utils.isEmpty(bodyString)) {
                    bodyString += " " + lastName;
                } else {
                    bodyString = lastName;
                }
            }

            if (!Utils.isEmpty(mShareCard.getPhone())) {
                bodyString += ", " + mShareCard.getPhone();
            }

            if (!Utils.isEmpty(mShareCard.getEmail())) {
                bodyString += ", " + mShareCard.getEmail();
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, bodyString);
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(shareIntent, "Поделиться..."));
        }

        return false;
    }

    public void updateView(View view, final Card card) {
        mShareCard = card;
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

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardUpdate(Card.OnCardUpdatedEvent event) {
        updateView(getView(), event.card);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.foto_block) {
            intent = new Intent(getContext(), PortfolioActivity.class);
            intent.putExtra(PortfolioActivity.EXTRA_TYPE, PortfolioActivity.EXTRA_TYPE_PHOTO);
        } else if (v.getId() == R.id.video_block) {
            intent = new Intent(getContext(), PortfolioActivity.class);
            intent.putExtra(PortfolioActivity.EXTRA_TYPE, PortfolioActivity.EXTRA_TYPE_VIDEO);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
