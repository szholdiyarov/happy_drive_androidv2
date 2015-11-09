package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.DrawerFragment;

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
    }

    @Override
    public boolean onDrawerMenuItemSelected(int itemId) {
        if (true) {
            throw new RuntimeException("This is an intentional crash for testing purpose");
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
