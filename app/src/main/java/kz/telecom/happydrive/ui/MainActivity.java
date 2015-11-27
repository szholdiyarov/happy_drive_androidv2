package kz.telecom.happydrive.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.fragment.*;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class MainActivity extends BaseActivity implements DrawerFragment.Callback {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

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

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, toolbar, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            User user = User.currentUser();
            if (user != null) {
                replaceContent(MainFragment.newInstance(user.card), false,
                        FragmentTransaction.TRANSIT_NONE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onDrawerMenuItemSelected(int itemId) {
        if (itemId == R.id.action_main) {
            if (!(findDefaultContent() instanceof MainFragment)) {
                replaceContent(MainFragment.newInstance(User.currentUser().card), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_card) {
            if (!(findDefaultContent() instanceof CardDetailsFragment)) {
                replaceContent(CardDetailsFragment.newInstance(User.currentUser().card), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_favourite) {
            if (!(findDefaultContent() instanceof StarFragment)) {
                replaceContent(new StarFragment(), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_catalog) {
            if (!(findDefaultContent() instanceof CatalogFragment)) {
                replaceContent(new CatalogFragment(), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_settings) {
            if (!(findDefaultContent() instanceof SettingsFragment)) {
                replaceContent(new SettingsFragment(), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_help) {
            if (!(findDefaultContent() instanceof HelpFragment)) {
                replaceContent(new HelpFragment(), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_about) {
            this.startActivity(new Intent(this, AboutActivity.class));
        }

        closeDrawer();
        return true;
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
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

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
            return;
        }

        BaseFragment fragment = findDefaultContent();
        if (fragment == null || !fragment.onBackPressed()) {
            if (!(fragment instanceof MainFragment)) {
                User user = User.currentUser();
                if (user != null) {
                    replaceContent(MainFragment.newInstance(user.card), false,
                            FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.activity_main_fmt_drawer);
                    if (drawerFragment != null) {
                        drawerFragment.setCheckedDrawerItemById(R.id.action_main);
                    }

                    return;
                }
            }

            super.onBackPressed();
        }
    }
}
