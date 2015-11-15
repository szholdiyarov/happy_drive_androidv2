package kz.telecom.happydrive.ui.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.BaseActivity;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class PasswordRecoveryFragment extends BaseFragment implements View.OnClickListener {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_recovery, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanteState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.password_recovery_title);

        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_accent);
        Button button = (Button) view.findViewById(R.id.fragment_password_recovery_btn);
        ViewCompat.setBackgroundTintList(button, tintList);
        button.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
    }
}
