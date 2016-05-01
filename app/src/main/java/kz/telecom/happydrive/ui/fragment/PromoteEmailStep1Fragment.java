package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;

/**
 * Created by shgalym on 4/10/16.
 */
public class PromoteEmailStep1Fragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promote_email_step_1, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_promote_email_step1_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (getParentFragment() instanceof PromoteEmailFragment) {
            ((PromoteEmailFragment) getParentFragment()).onStepProceed(this, null);
        }
    }
}
