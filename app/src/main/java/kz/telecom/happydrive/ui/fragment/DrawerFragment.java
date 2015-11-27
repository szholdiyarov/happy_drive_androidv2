package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
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
import kz.telecom.happydrive.util.GlideRoundedCornersTransformation;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 10/29/15.
 */
public class DrawerFragment extends BaseFragment {
    private ImageView mBackgroundImageView;
    private ImageView mPhotoImageView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;

    private Callback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final NavigationView navi = (NavigationView) view.findViewById(R.id.fragment_main_navigation_view);
        View headerView = navi.inflateHeaderView(R.layout.layout_drawer_header);
        mBackgroundImageView = (ImageView) headerView.findViewById(R.id.drawer_header_background_img);
        mPhotoImageView = (ImageView) headerView.findViewById(R.id.drawer_header_photo);
        mUsernameTextView = (TextView) headerView.findViewById(R.id.drawer_header_username);
        mEmailTextView = (TextView) headerView.findViewById(R.id.drawer_header_email);

        navi.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return mCallback.onDrawerMenuItemSelected(item.getItemId());
            }
        });

        User user = User.currentUser();
        if (user != null) {
            updateHeaderState(user.card);
        }

        DataManager.getInstance().bus.register(this);
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
        if (event.card.compareTo(User.currentUser().card) == 0) {
            updateHeaderState(event.card);
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

        mPhotoImageView.post(new Runnable() {
            @Override
            public void run() {
                if (!Utils.isEmpty(card.getAvatar())) {
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    NetworkManager.getGlide()
                            .load(card.getAvatar())
                            .signature(GlideCacheSignature
                                    .ownerAvatarKey(card.getAvatar()))
                            .error(R.drawable.user_photo)
                            .bitmapTransform(new CenterCrop(getContext()),
                                    new GlideRoundedCornersTransformation(getContext(),
                                            Utils.dipToPixels(6f, dm), Utils.dipToPixels(2f, dm)))
                            .override(mPhotoImageView.getWidth(),
                                    mPhotoImageView.getHeight())
                            .into(mPhotoImageView);

                } else {
                    mPhotoImageView.setImageResource(R.drawable.user_photo);
                }

                if (!Utils.isEmpty(card.getBackground())) {
                    NetworkManager.getGlide()
                            .load(card.getBackground())
                            .signature(GlideCacheSignature
                                    .ownerBackgroundKey(card.getBackground()))
                            .override(mBackgroundImageView.getWidth(),
                                    mBackgroundImageView.getHeight())
                            .into(mBackgroundImageView);
                } else {
                    mBackgroundImageView.setImageDrawable(null);
                }
            }
        });
    }

    public interface Callback {
        boolean onDrawerMenuItemSelected(int itemId);
    }
}
