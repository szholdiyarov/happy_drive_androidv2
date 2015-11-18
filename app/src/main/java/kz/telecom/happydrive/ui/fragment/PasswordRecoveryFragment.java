package kz.telecom.happydrive.ui.fragment;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class PasswordRecoveryFragment extends BaseFragment implements View.OnClickListener {
    private EditText mEmailEditText;
    private Button mButton;

    private boolean isProcessing = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
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

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_password_recovery_et_email);

        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_accent);
        mButton = (Button) view.findViewById(R.id.fragment_password_recovery_btn);
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
        if (view.getId() == R.id.fragment_password_recovery_btn) {
            restorePassword(mEmailEditText.getText().toString());
        }
    }

    private void restorePassword(final String email) {
        toggleViewStates(isProcessing = true);
        new Thread() {
            @Override
            public void run() {
                try {
                    User.restorePassword(email);
                    final BaseActivity activity = (BaseActivity) getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().bus
                                        .post(new User.PasswordRestoredEvent());
                                showSuccessfulMessage(true);
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
                                                    restorePassword(email);
                                                }
                                            }).show();
                                } else if (e instanceof ApiResponseError) {
                                    showSuccessfulMessage(true);
                                }
                            }
                        });
                    }
                }

                toggleViewStates(isProcessing = false);
            }
        }.start();
    }

    private void showSuccessfulMessage(final boolean shouldGoBack) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.password_recovery_title)
                .setMessage(R.string.password_recovery_successful_msg)
                .setPositiveButton(R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (shouldGoBack && getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }
                }).show();
    }

    private void toggleViewStates(final boolean processing) {
        BaseActivity activity = (BaseActivity) getActivity();
        if (activity != null && mButton != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mButton.setEnabled(!processing);
                    mEmailEditText.setEnabled(!processing);
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
