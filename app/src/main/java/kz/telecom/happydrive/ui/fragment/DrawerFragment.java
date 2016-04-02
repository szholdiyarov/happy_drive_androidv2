package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 10/29/15.
 */
public class DrawerFragment extends BaseFragment {
    private NavigationView mNavigationView;
    private ImageView mBackgroundImageView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;
    private TextView mStorageSizeTextView;
    private TextView mExpirationDateTextView;

    private Callback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mNavigationView = (NavigationView) view.findViewById(R.id.fragment_main_navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return mCallback.onDrawerMenuItemSelected(item.getItemId());
            }
        });

        View headerView = mNavigationView.inflateHeaderView(R.layout.layout_drawer_header);
        mBackgroundImageView = (ImageView) headerView.findViewById(R.id.drawer_header_background_img);
        mUsernameTextView = (TextView) headerView.findViewById(R.id.drawer_header_username);
        mEmailTextView = (TextView) headerView.findViewById(R.id.drawer_header_email);

        mStorageSizeTextView = (TextView) view.findViewById(R.id.fragment_drawer_tv_storage_size);
        mExpirationDateTextView = (TextView) view.findViewById(R.id.fragment_drawer_tv_expiration_date);

        final User user = User.currentUser();
        if (user != null) {
            updateHeaderState(user.card);
            updateFooterState(user);

            new Thread() {
                @Override
                public void run() {
                    try {
                        user.updateStorageSize();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mStorageSizeTextView != null && getView() != null) {
                                        updateFooterState(user);
                                    }
                                }
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }
            }.start();
        }

        setCheckedDrawerItemById(R.id.action_card);
        DataManager.getInstance().bus.register(this);
    }

    public void setCheckedDrawerItemById(int resId) {
        mNavigationView.setCheckedItem(resId);
    }

    @Override
    public void onDestroyView() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        try {
            mCallback = (MainActivity) context;
        } catch (ClassCastException ignored) {
            throw new IllegalStateException("Context must implement " +
                    DrawerFragment.class.getSimpleName() + "." + Callback.class.getSimpleName());
        }

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardUpdate(Card.OnCardUpdatedEvent event) {
        if (event.card.compareTo(User.currentUser().card) == 0
                && getView() != null) {
            updateHeaderState(event.card);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardBackgroundUpdate(Card.OnBackgroundUpdatedEvent event) {
        if (event.card.compareTo(User.currentUser().card) == 0
                && getView() != null) {
            updateBackground(event.card);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onStorageSizeUpdated(User.OnStorageSizeUpdatedEvent event) {
        if (User.currentUser() != null && getView() != null) {
            updateFooterState(User.currentUser());
        }
    }

    private void updateHeaderState(final Card card) {
        String lastName = card.getLastName();
        String username = card.getFirstName();
        if (!Utils.isEmpty(lastName)) {
            if (!Utils.isEmpty(username)) {
                username += " " + lastName;
            } else {
                username = lastName;
            }
        }

        mUsernameTextView.setText(username);
        mEmailTextView.setText(card.getEmail());
        updateBackground(card);
    }

    private void updateBackground(@NonNull Card card) {
        NetworkManager.getGlide()
                .load(card.getBackground())
                .signature(GlideCacheSignature
                        .ownerBackgroundKey(card.getBackground()))
                .placeholder(R.drawable.bkg_auth)
                .error(R.drawable.bkg_auth)
                .centerCrop()
                .into(mBackgroundImageView);
    }

    private void updateFooterState(User user) {
        mStorageSizeTextView.setText(getString(R.string.drawer_footer_storage_size_fmt,
                Formatter.formatShortFileSize(getContext(), user.getStorageUsed()),
                Formatter.formatShortFileSize(getContext(), user.getStorageTotal())));
        final String expirationDate = user.card.getExpirationDate();
        if (expirationDate != null) {
            try {
                Calendar date = Calendar.getInstance();
                date.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(expirationDate));
                Calendar now = Calendar.getInstance();

                int days = 0;
                while (now.before(date)) {
                    now.add(Calendar.DAY_OF_MONTH, 1);
                    days++;
                }

                mExpirationDateTextView.setText(getString(R.string.drawer_footer_expiration_fmt, days));
            } catch (Exception ignored) {
            }
        }
    }

    public interface Callback {
        boolean onDrawerMenuItemSelected(int itemId);
    }
}
