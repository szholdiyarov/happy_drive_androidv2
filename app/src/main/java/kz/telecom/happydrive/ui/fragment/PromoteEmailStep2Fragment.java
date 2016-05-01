package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;

/**
 * Created by shgalym on 4/10/16.
 */
public class PromoteEmailStep2Fragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_email_step_2, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onClick(View view) {
        if (getParentFragment() instanceof PromoteEmailFragment) {
            ((PromoteEmailFragment) getParentFragment()).onStepProceed(this, null);
        }
    }
}
