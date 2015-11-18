package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class SignUpFragment extends BaseFragment implements View.OnClickListener {
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mButton;

    boolean isProcessing = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.sign_up_title);

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_sign_up_et_email);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_sign_up_et_password);

        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_accent);
        mButton = (Button) view.findViewById(R.id.fragment_sign_up_btn);
        ViewCompat.setBackgroundTintList(mButton, tintList);
        mButton.setOnClickListener(this);

        toggleViewStates(isProcessing);
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
        if (view.getId() == R.id.fragment_sign_up_btn) {
            signUp(mEmailEditText.getText().toString(),
                    mPasswordEditText.getText().toString());
        }
    }

    private void signUp(final String email, final String password) {
        toggleViewStates(isProcessing = true);
        new Thread() {
            @Override
            public void run() {
                try {
                    final User user = User.signUp(email, password);
                    final BaseActivity activity = (BaseActivity) getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().bus
                                        .post(new User.SignedUpEvent(user));
                                activity.startActivity(new Intent(activity,
                                        MainActivity.class));
                            }
                        });
                    }
                } catch (final Exception e) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e instanceof NoConnectionError) {
                                    Snackbar.make(view, R.string.no_connection, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.retry, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    signUp(email, password);
                                                }
                                            }).show();
                                } else if (e instanceof ApiResponseError) {
                                    int errorResId = R.string.sign_up_user_cred_invalid;
                                    if (((ApiResponseError) e).apiErrorCode ==
                                            ApiResponseError.API_RESPONSE_CODE_USER_ALREADY_EXISTS) {
                                        errorResId = R.string.sign_up_user_already_taken;
                                    }

                                    Snackbar.make(view, errorResId, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }

                toggleViewStates(isProcessing = false);
            }
        }.start();
    }

    private void toggleViewStates(final boolean processing) {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null && mButton != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mButton.setEnabled(!processing);
                    mEmailEditText.setEnabled(!processing);
                    mPasswordEditText.setEnabled(!processing);
                }
            };

            if (Looper.myLooper() != Looper.getMainLooper()) {
                activity.runOnUiThread(runnable);
            } else {
                runnable.run();
            }
        }
    }
}
