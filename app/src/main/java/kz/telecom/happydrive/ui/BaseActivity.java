package kz.telecom.happydrive.ui;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kz.telecom.happydrive.ui.fragment.BaseFragment;

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

    public void replaceContent(@NonNull BaseFragment fragment,
            boolean stackable, @Transit int transition) {
        @IdRes int viewContainerId = getDefaultContentViewContainerId();
        if (0 >= viewContainerId) {
            throw new IllegalStateException("Activity must override getDefaultContentViewContainerId()" +
                    "in order to call this method");
        }

        replaceContent(viewContainerId, fragment, stackable, transition);
    }

    protected void replaceContent(@IdRes int viewContainerId,
            @NonNull BaseFragment fragment, boolean stackable) {
        replaceContent(viewContainerId, fragment, stackable, FragmentTransaction.TRANSIT_NONE);
    }

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
    protected BaseFragment findContent(@IdRes int viewContainerId) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(viewContainerId);
        return fragment instanceof BaseFragment ? (BaseFragment) fragment : null;
    }

    @IdRes
    protected  int getDefaultContentViewContainerId() {
        return -1;
    }

    @NonNull
    protected ActionBar initToolbar(int toolbarId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        if (toolbar == null) {
            throw new Resources.NotFoundException("Toolbar not found");
        }

        return initToolbar(toolbar);
    }

    @NonNull
    protected ActionBar initToolbar(Toolbar toolbar) {
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

    @SuppressWarnings("unused")
    protected void deinitToolbar() {
        setSupportActionBar(null);
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = findContent(getDefaultContentViewContainerId());
        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
