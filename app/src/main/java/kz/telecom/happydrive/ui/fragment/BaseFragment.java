package kz.telecom.happydrive.ui.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by Galymzhan Sh on 10/27/15.
 */
public abstract class BaseFragment extends Fragment {
    public boolean onBackPressed() {
        return false;
    }
}
