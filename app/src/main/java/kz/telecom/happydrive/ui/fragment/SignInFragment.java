package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
public class SignInFragment extends BaseFragment implements View.OnClickListener {
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mButton;

    private boolean isProcessing = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanteState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.sign_in_title);

        ColorStateList signInTintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_accent);
        mButton = (Button) view.findViewById(R.id.fragment_sign_in_btn_sign_in);
        ViewCompat.setBackgroundTintList(mButton, signInTintList);
        mButton.setOnClickListener(this);

        TextView textView = (TextView) view.findViewById(R.id.fragment_sign_in_tv_sign_up);
        textView.setText(textView.getText(), TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                ((BaseActivity) getActivity()).replaceContent(
                        new SignUpFragment(), true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
        };

        Spannable spannable = (Spannable) textView.getText();
        final String text = textView.getText().toString();
        final String signUp = "Регистрация";
        final int startIdx = text.indexOf(signUp);
        final int endIdx = startIdx + signUp.length();
        spannable.setSpan(clickableSpan, startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mEmailEditText = (EditText) view.findViewById(R.id.fragment_sign_in_et_email);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_sign_in_et_password);
        view.findViewById(R.id.fragment_sign_in_tv_password_recovery)
                .setOnClickListener(this);

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
        final int viewId = view.getId();
        if (viewId == R.id.fragment_sign_in_btn_sign_in) {
            signIn(mEmailEditText.getText().toString(),
                    mPasswordEditText.getText().toString());
        } else if (viewId == R.id.fragment_sign_in_tv_password_recovery) {
            ((BaseActivity) getActivity()).replaceContent(
                    new PasswordRecoveryFragment(), true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    }

    private void signIn(final String email, final String password) {
        toggleViewStates(isProcessing = true);
        new Thread() {
            @Override
            public void run() {
                try {
                    final User user = User.signIn(email, password);
                    final BaseActivity activity = (BaseActivity) getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DataManager.getInstance().bus
                                        .post(new User.SignedInEvent(user));
                                activity.startActivity(new Intent(activity,
                                        MainActivity.class));
                            }
                        });
                        activity.finish();
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
                                                    signIn(email, password);
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
