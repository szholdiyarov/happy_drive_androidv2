package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;

/**
 * Created by shgalym on 4/10/16.
 */
public class PromoteEmailFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_email, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (savedInstanceState == null || getChildFragmentManager()
                .findFragmentById(R.id.fragment_promote_email_view_container) == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_promote_email_view_container,
                            new PromoteEmailStep1Fragment())
                    .commit();
        }
    }

    void onStepProceed(BaseFragment fragment, Object data) {
        if (fragment instanceof PromoteEmailStep1Fragment) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_promote_email_view_container,
                            new PromoteEmailStep2Fragment())
                    .commit();
        }
    }
}
