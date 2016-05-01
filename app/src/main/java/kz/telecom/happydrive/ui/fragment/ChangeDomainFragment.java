package kz.telecom.happydrive.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import kz.telecom.happydrive.data.ApiClient;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.ResponseParseError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.ChangeDomainActivity;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 19.12.2015.
 */
public class ChangeDomainFragment extends BaseFragment implements View.OnClickListener {
    private EditText mDomainEditText;
    private Button mButton;

    private Snackbar mSnackbar;
    private boolean mIsProcessing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_domain, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Установить домен");

        mDomainEditText = (EditText) view.findViewById(R.id.fragment_change_domain_et);
        mDomainEditText.setText(User.currentUser().card.getDomain());

        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_primary);
        mButton = (Button) view.findViewById(R.id.fragment_change_domain_btn);
        ViewCompat.setBackgroundTintList(mButton, tintList);
        mButton.setOnClickListener(this);

        toggleViewStates(!mIsProcessing);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final String domain = mDomainEditText.getText().toString();
        if (Utils.isEmpty(domain)) {
            mDomainEditText.setError("Обязательное поле");
            return;
        } else if (!Utils.isDomain(domain)) {
            mDomainEditText.setError("Неправильный формат");
            return;
        }

        changeDomain(domain);
    }

    private synchronized void changeDomain(@NonNull final String domain) {
        if (mIsProcessing) {
            return;
        }

        mIsProcessing = true;
        toggleViewStates(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    ApiClient.changeDomain(domain);
                    User.currentUser().card.setDomain(domain);
                    final Activity activity = getActivity();
                    if (activity instanceof ChangeDomainActivity) {
                        activity.finish();
                    }
                } catch (NoConnectionError | ApiResponseError | ResponseParseError e) {
                    final View view = getView();
                    if (view != null) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                if (e instanceof NoConnectionError) {
                                    mSnackbar = Snackbar.make(view, R.string.no_connection,
                                            Snackbar.LENGTH_LONG);
                                    mSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            changeDomain(domain);
                                        }
                                    });
                                } else if (e instanceof ApiResponseError) {
                                    if (((ApiResponseError) e).apiErrorCode == 18) {
                                        mSnackbar = Snackbar.make(view, "Этот домен уже занят",
                                                Snackbar.LENGTH_LONG);
                                    }
                                } else {
                                    mSnackbar = Snackbar.make(view, "Произошла неизвестная ошибка",
                                            Snackbar.LENGTH_LONG);
                                }

                                mSnackbar.show();
                            }
                        });
                    }
                }

                toggleViewStates(true);
                mIsProcessing = false;
            }
        }.start();
    }

    private void toggleViewStates(final boolean enabled) {
        if (getView() != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mDomainEditText.setEnabled(enabled);
                    mButton.setError(null);

                    if (!enabled && mSnackbar != null && mSnackbar.isShownOrQueued()) {
                        mSnackbar.dismiss();
                        mSnackbar = null;
                    }
                }
            };

            if (Looper.myLooper() != Looper.getMainLooper()) {
                getActivity().runOnUiThread(runnable);
            } else {
                runnable.run();
            }
        }
    }
}
