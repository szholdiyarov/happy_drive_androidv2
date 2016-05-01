package kz.telecom.happydrive.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import java.util.List;

import kz.telecom.happydrive.BuildConfig;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.Card;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.GlideCacheSignature;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.ui.fragment.BaseFragment;
import kz.telecom.happydrive.ui.fragment.CardDetailsFragment;
import kz.telecom.happydrive.ui.fragment.CatalogFragment;
import kz.telecom.happydrive.ui.fragment.CloudFragment;
import kz.telecom.happydrive.ui.fragment.DrawerFragment;
import kz.telecom.happydrive.ui.fragment.MainFragment;
import kz.telecom.happydrive.ui.fragment.StarFragment;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public class MainActivity extends BaseActivity implements DrawerFragment.Callback {
    private DrawerLayout mDrawerLayout;
    private ImageView mBackgroundImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackgroundImageView = (ImageView) findViewById(R.id.activity_main_img_view_background);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mDrawerLayout.setFitsSystemWindows(false);
        }

        User user = User.currentUser();
        if (savedInstanceState == null) {
            if (user != null) {
                replaceContent(new MainFragment(), false,
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

        if (user != null) {
            updateBackground(user.card);
        }

        DataManager.getInstance().bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataManager.getInstance().bus.unregister(this);
    }

    @Override
    public boolean onDrawerMenuItemSelected(int itemId) {
        if (itemId == R.id.action_main) {
            if (!(findDefaultContent() instanceof MainFragment)) {
                replaceContent(new MainFragment(), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        } else if (itemId == R.id.action_card) {
            if (!(findDefaultContent() instanceof CardDetailsFragment)) {
                replaceContent(CardDetailsFragment.newInstance(User.currentUser().card, false), false,
                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
//        } else if (itemId == R.id.action_cloud) {
//            if (!(findDefaultContent() instanceof CloudFragment)) {
//                replaceContent(new CloudFragment(), false,
//                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            }
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
            startActivity(new Intent(this, SettingsActivity.class));
//        } else if (itemId == R.id.action_help) {
//            if (!(findDefaultContent() instanceof HelpFragment)) {
//                replaceContent(new HelpFragment(), false,
//                        FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            }
//        } else if (itemId == R.id.action_about) {
//            this.startActivity(new Intent(this, AboutActivity.class));
        } else if (itemId == R.id.action_promote) {
            startActivity(new Intent(this, PromoteActivity.class));
        }

        closeDrawer();
        return true;
    }

    // TODO slow call, optimize
    public void drawerMenuItemSelect(int itemId) {
        if (onDrawerMenuItemSelected(itemId)) {
            DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.activity_main_fmt_drawer);
            if (drawerFragment != null) {
                drawerFragment.setCheckedDrawerItemById(itemId);
            }
        }
    }

    @SuppressWarnings("unused")
    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @SuppressWarnings("unused")
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("unused")
    public void toggleDrawer() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onCardBackgroundChanged(Card.OnBackgroundUpdatedEvent event) {
        updateBackground(event.card);
    }

    private void updateBackground(@NonNull Card card) {
        NetworkManager.getGlide()
                .load(card.getBackground())
                .signature(GlideCacheSignature.ownerBackgroundKey(card.getBackground()))
                .placeholder(R.drawable.default_bkg)
                .error(R.drawable.default_bkg)
                .into(mBackgroundImageView);
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
                    replaceContent(new MainFragment(), false,
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
