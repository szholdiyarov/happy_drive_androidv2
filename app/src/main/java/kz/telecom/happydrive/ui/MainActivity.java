package kz.telecom.happydrive.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import kz.telecom.happydrive.BuildConfig;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.fragment.BaseFragment;
import kz.telecom.happydrive.ui.fragment.CardDetailsFragment;
import kz.telecom.happydrive.ui.fragment.CatalogFragment;
import kz.telecom.happydrive.ui.fragment.CloudFragment;
import kz.telecom.happydrive.ui.fragment.DrawerFragment;
import kz.telecom.happydrive.ui.fragment.HelpFragment;
import kz.telecom.happydrive.ui.fragment.SettingsFragment;
import kz.telecom.happydrive.ui.fragment.StarFragment;
import kz.telecom.happydrive.ui.widget.BackgroundChangeable;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class MainActivity extends BaseActivity
        implements DrawerFragment.Callback, BackgroundChangeable {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mBackgroundImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackgroundImageView = (ImageView) findViewById(R.id.activity_main_img_view_background);
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
                replaceContent(CardDetailsFragment.newInstance(user.card), false,
                        FragmentTransaction.TRANSIT_NONE);
            }

            ParseQuery<ParseObject> query = new ParseQuery<>("Version");
            query.whereEqualTo("platform", "android");
            query.whereGreaterThanOrEqualTo("versionCode", BuildConfig.VERSION_CODE);
            query.addAscendingOrder("versionCode");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null) {
                        Logger.e("PARSE", e.getLocalizedMessage(), e);
                        return;
                    }

                    boolean updateFound = false;
                    boolean isMandatory = false;
                    for (ParseObject obj : objects) {
                        int versionCode = obj.getInt("versionCode");
                        if (versionCode > BuildConfig.VERSION_CODE) {
                            updateFound = true;
                            if (obj.getBoolean("isMandatory")) {
                                isMandatory = true;
                                break;
                            }
                        }
                    }

                    if (updateFound) {
                        if (isMandatory) {
                            Intent intent = new Intent(MainActivity.this, LockedActivity.class);
                            intent.putExtra(LockedActivity.EXTRA_CAUSE, LockedActivity.CAUSE_UPDATE_REQUIRED);
                            startActivity(intent);
                            finish();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Обновление")
                                    .setMessage("Доступно новое обновление приложения. Хотите обновить?")
                                    .setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            final String packageName = getPackageName();
                                            Utils.goToAppStore(MainActivity.this, packageName);
                                        }
                                    }).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onDrawerMenuItemSelected(int itemId) {
        if (itemId == R.id.action_card) {
            if (!(findDefaultContent() instanceof CardDetailsFragment)) {
                replaceContent(CardDetailsFragment.newInstance(User.currentUser().card), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_cloud) {
            if (!(findDefaultContent() instanceof CloudFragment)) {
                replaceContent(new CloudFragment(), false,
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
//        } else if (itemId == R.id.action_about) {
//            this.startActivity(new Intent(this, AboutActivity.class));
        }

        closeDrawer();
        return true;
    }

    @Override
    public void changeBackground(Bitmap bitmap) {
        mBackgroundImageView.setImageBitmap(bitmap);
    }

    @Override
    public ImageView getBackgroundImageView() {
        return mBackgroundImageView;
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
            if (!(fragment instanceof CardDetailsFragment)) {
                User user = User.currentUser();
                if (user != null) {
                    replaceContent(CardDetailsFragment.newInstance(user.card), false,
                            FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.activity_main_fmt_drawer);
                    if (drawerFragment != null) {
                        drawerFragment.setCheckedDrawerItemById(R.id.action_card);
                    }

                    return;
                }
            }

            super.onBackPressed();
        }
    }
}
