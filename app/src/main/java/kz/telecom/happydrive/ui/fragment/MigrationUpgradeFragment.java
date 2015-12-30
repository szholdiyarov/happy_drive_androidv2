package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 12/3/15.
 */
public class MigrationUpgradeFragment extends BaseFragment implements View.OnClickListener {
    private String mLogin;
    private String mPassword;
    private EditText mEmailEditText;
    private Button mButton;

    private boolean isProcessing = false;

    public static BaseFragment newInstance(String login, String password) {
        Bundle bundle = new Bundle();
        bundle.putString("login", login);
        bundle.putString("password", password);
        BaseFragment fragment = new MigrationUpgradeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_migration_upgrade, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Сохранить новый E-mail");

        mLogin = getArguments().getString("login");
        mPassword = getArguments().getString("password");

        ColorStateList signInTintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_accent);
        mButton = (Button) view.findViewById(R.id.fragment_migration_upgrade_btn_sign_in);
        ViewCompat.setBackgroundTintList(mButton, signInTintList);
        mButton.setOnClickListener(this);

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_migration_upgrade_et_email);

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
    public void onClick(View v) {
        final String email = mEmailEditText.getText().toString();
        if (Utils.isEmpty(email)) {
            mEmailEditText.setError("Обязательное поле");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("Не похож на E-mail");
            return;
        }

        migrate(mLogin, mPassword, email);
    }

    private void migrate(final String login, final String password, final String email) {
        toggleViewStates(isProcessing = true);
        new Thread() {
            @Override
            public void run() {
                try {
                    final User user = User.migrate(login, password, email, password);
                    final BaseActivity activity = (BaseActivity) getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().bus
                                        .post(new User.SignedInEvent(user));
                                activity.startActivity(new Intent(activity,
                                        MainActivity.class));
                                activity.finish();
                            }
                        });
                    }
                } catch (final Exception e) {
                    final BaseActivity activity = (BaseActivity) getActivity();
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
                                                    migrate(login, password, email);
                                                }
                                            }).show();
                                } else if (e instanceof ApiResponseError) {
                                    Snackbar.make(view, R.string.sign_in_user_cred_invalid, Snackbar.LENGTH_LONG)
                                            .setDuration(Snackbar.LENGTH_LONG)
                                            .show();
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
