package kz.telecom.happydrive.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kz.telecom.happydrive.R;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class SignInFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanteState) {
    }
}
