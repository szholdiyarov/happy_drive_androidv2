package kz.telecom.happydrive.ui.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.BaseActivity;

/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class AuthFragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn);
        Button signInButton = (Button) view.findViewById(R.id.fragment_auth_btn_sign_in);
        ViewCompat.setBackgroundTintList(signInButton, tintList);
        signInButton.setOnClickListener(this);

        Button signUpButton = (Button) view.findViewById(R.id.fragment_auth_btn_sign_up);
        ViewCompat.setBackgroundTintList(signUpButton, tintList);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        BaseFragment fragment = null;
        if (viewId == R.id.fragment_auth_btn_sign_in) {
            fragment = new SignInFragment();
        } else if (viewId == R.id.fragment_auth_btn_sign_up) {
            fragment = new SignUpFragment();
        }

        if (fragment != null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.replaceContent(fragment, true,
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    }
}