package kz.telecom.happydrive.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import kz.telecom.happydrive.ui.BaseActivity;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public abstract class BaseFragment extends Fragment {
    @Override
    public void onAttach(Context context) {
        if (!(context instanceof BaseActivity)) {
            throw new IllegalStateException(this.getClass().getSimpleName() +
                    " is used to work with " + BaseActivity.class.getSimpleName());
        }

        super.onAttach(context);
    }

    public boolean onBackPressed() {
        return false;
    }
}
