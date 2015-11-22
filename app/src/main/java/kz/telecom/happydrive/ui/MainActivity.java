package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.fragment.CardDetailsFragment;
import kz.telecom.happydrive.ui.fragment.DrawerFragment;
import kz.telecom.happydrive.ui.fragment.MainFragment;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class MainActivity extends BaseActivity implements DrawerFragment.Callback {
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_toolbar);
        initToolbar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mDrawerLayout.setFitsSystemWindows(false);
        }

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, 0, 0);
        mDrawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (savedInstanceState == null) {
            Card card = null;
            User user = User.currentUser();
            if (user != null) {
                card = user.card;
            }

            replaceContent(CardDetailsFragment.newInstance(card), false,
                    FragmentTransaction.TRANSIT_NONE);
        }
    }

    @Override
    public boolean onDrawerMenuItemSelected(int itemId) {
        if (itemId == R.id.action_main) {
            if (!(findDefaultContent() instanceof MainFragment)) {
                replaceContent(MainFragment.newInstance(User.currentUser().card), true,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_card) {
            if (!(findDefaultContent() instanceof CardDetailsFragment)) {
                replaceContent(CardDetailsFragment.newInstance(User.currentUser().card), true,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        }

        closeDrawer();
        return true;
    }

    @SuppressWarnings("unused")
    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @SuppressWarnings("unused")
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected int getDefaultContentViewContainerId() {
        return R.id.activity_main_view_container;
    }
}
