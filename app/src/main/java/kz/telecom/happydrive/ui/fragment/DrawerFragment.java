package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.MainActivity;

/**
 * Created by Galymzhan Sh on 10/29/15.
 */
public class DrawerFragment extends BaseFragment {
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
        mPhotoImageView = (ImageView) headerView.findViewById(R.id.drawer_header_photo);
        mUsernameTextView = (TextView) headerView.findViewById(R.id.drawer_header_username);
        mEmailTextView = (TextView) headerView.findViewById(R.id.drawer_header_email);

        navi.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return item.isCheckable() && mCallback.onDrawerMenuItemSelected(item.getItemId());
            }
        });

        updateHeaderState();
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
    public void onCardUpdate(Card.OnCardUpdateEvent ignored) {
        updateHeaderState();
    }

    private void updateHeaderState() {
        User user = User.currentUser();
        if (user != null) {
            mEmailTextView.setText(user.email);
        }

        Card card = Card.getUserCard(getContext());
        if (card != null) {
            mUsernameTextView.setText(card.firstName + " " +
                    card.lastName + " " + card.middleName);
        }
    }

    public interface Callback {
        boolean onDrawerMenuItemSelected(int itemId);
    }
}
