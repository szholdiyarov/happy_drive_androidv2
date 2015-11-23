package kz.telecom.happydrive.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Subscribe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.fragment.BaseFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public abstract class BaseActivity extends AppCompatActivity {
    @IntDef({FragmentTransaction.TRANSIT_NONE,
            FragmentTransaction.TRANSIT_FRAGMENT_OPEN,
            FragmentTransaction.TRANSIT_FRAGMENT_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Transit {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(this instanceof AuthActivity) && !User.isAuthenticated()) {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

        DataManager.getInstance().bus.register(this);
    }

    @Override
    public void onDestroy() {
        DataManager.getInstance().bus.unregister(this);
        super.onDestroy();
    }

    @NonNull
    @SuppressWarnings("unused")
    public ActionBar initToolbar(@IdRes int toolbarId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        if (toolbar == null) {
            throw new Resources.NotFoundException("Toolbar not found");
        }

        return initToolbar(toolbar);
    }

    @NonNull
    @SuppressWarnings("unused")
    public ActionBar initToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            throw new IllegalArgumentException("toolbar is null");
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new IllegalStateException("Internal error: toolbar couldn't be settled");
        }

        return actionBar;
    }

    public void replaceContent(@NonNull BaseFragment fragment,
            boolean stackable, @Transit int transition) {
        @IdRes int viewContainerId = getDefaultContentViewContainerId();
        if (0 >= viewContainerId) {
            throw new IllegalStateException("Activity must override getDefaultContentViewContainerId()" +
                    "in order to call this method");
        }

        replaceContent(viewContainerId, fragment, stackable, transition);
    }

    @SuppressWarnings("unused")
    protected void replaceContent(@IdRes int viewContainerId,
            @NonNull BaseFragment fragment, boolean stackable) {
        replaceContent(viewContainerId, fragment, stackable, FragmentTransaction.TRANSIT_NONE);
    }

    @SuppressWarnings("unused")
    protected void replaceContent(@IdRes int viewContainerId, @NonNull BaseFragment fragment,
            boolean stackable, @Transit int transition) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewContainerId, fragment);
        transaction.setTransition(transition);
        if (stackable) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Nullable
    @SuppressWarnings("unused")
    public BaseFragment findDefaultContent() {
        @IdRes int viewContainerId = getDefaultContentViewContainerId();
        if (0 >= viewContainerId) {
            throw new IllegalStateException("Activity must override getDefaultContentViewContainerId()" +
                    "in order to call this method");
        }

        return findContent(viewContainerId);
    }

    @Nullable
    @SuppressWarnings("unused")
    protected BaseFragment findContent(@IdRes int viewContainerId) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(viewContainerId);
        return fragment instanceof BaseFragment ? (BaseFragment) fragment : null;
    }

    @IdRes
    @SuppressWarnings("unused")
    protected int getDefaultContentViewContainerId() {
        return -1;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onUserSignedOut(User.SignedOutEvent ignored) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = findContent(getDefaultContentViewContainerId());
        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

}
