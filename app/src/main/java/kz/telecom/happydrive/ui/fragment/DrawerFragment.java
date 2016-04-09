package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.squareup.otto.Subscribe;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.util.GlideDoubleRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 10/29/15.
 */
public class DrawerFragment extends BaseFragment {
    private NavigationView mNavigationView;
    private ImageView mPhotoImageView;
    private TextView mNameTextView;
    private TextView mPositionTextView;
    private TextView mStorageSizeTextView;

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
        mPhotoImageView = (ImageView) headerView.findViewById(R.id.drawer_header_photo);
        mNameTextView = (TextView) headerView.findViewById(R.id.drawer_header_name);
        mPositionTextView = (TextView) headerView.findViewById(R.id.drawer_header_position);

        mStorageSizeTextView = (TextView) view.findViewById(R.id.fragment_drawer_tv_storage_size);

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
    public void onCardAvatarUpdated(Card.OnAvatarUpdatedEvent event) {
        if (event.card.compareTo(User.currentUser().card) == 0
                && getView() != null) {
            updatePhoto(event.card);
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

        String position = card.getPosition();
        if (position != null) {
            position = position.toUpperCase();
        }

        mNameTextView.setText(username);
        mPositionTextView.setText(position);
        updatePhoto(card);
    }

    private void updatePhoto(@NonNull Card card) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        NetworkManager.getGlide()
                .load(card.getAvatar())
                .signature(GlideCacheSignature
                        .ownerAvatarKey(card.getAvatar()))
                .placeholder(R.drawable.ic_drawer_placeholder)
                .error(R.drawable.ic_drawer_placeholder)
                .bitmapTransform(new CenterCrop(getContext()),
                        new GlideDoubleRoundedCornersTransformation(getContext(),
                                Utils.dipToPixels(32f, dm),
                                Utils.dipToPixels(7f, dm), Color.parseColor("#3c3b56"),
                                Utils.dipToPixels(2f, dm), Color.WHITE))
                .into(mPhotoImageView);
    }

    private void updateFooterState(User user) {
        mStorageSizeTextView.setText(getString(R.string.drawer_footer_storage_size_fmt,
                Formatter.formatShortFileSize(getContext(), user.getStorageUsed()),
                Formatter.formatShortFileSize(getContext(), user.getStorageTotal())));
    }

    public interface Callback {
        boolean onDrawerMenuItemSelected(int itemId);
    }
}
