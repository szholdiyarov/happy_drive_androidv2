package kz.telecom.happydrive.ui.fragment;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;
import kz.telecom.happydrive.util.Logger;


/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class AuthFragment extends BaseFragment implements View.OnClickListener,
        FacebookCallback<LoginResult>, GoogleApiClient.OnConnectionFailedListener {
    private static final int GOOGLE_SIGN_IN = 999;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ColorStateList tintList = ContextCompat.getColorStateList(getContext(), R.color.auth_btn_primary);
        Button signInButton = (Button) view.findViewById(R.id.fragment_auth_btn_sign_in);
        ViewCompat.setBackgroundTintList(signInButton, tintList);
        signInButton.setOnClickListener(this);

        Button signUpButton = (Button) view.findViewById(R.id.fragment_auth_btn_sign_up);
        ViewCompat.setBackgroundTintList(signUpButton, tintList);
        signUpButton.setOnClickListener(this);

        SignInButton gSignInButton = (SignInButton) view.findViewById(R.id.google_login_button);
        gSignInButton.setSize(SignInButton.SIZE_WIDE);
        gSignInButton.setOnClickListener(this);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, this);

//        loginButton.
        // Set appropriate icon size.
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 1.45F),
                        (int) (drawable.getIntrinsicHeight() * 1.45F));
        loginButton.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public void onClick(View view) {
        final int viewId = view.getId();
        BaseFragment fragment = null;
        if (viewId == R.id.fragment_auth_btn_sign_in) {
            fragment = new SignInFragment();
        } else if (viewId == R.id.fragment_auth_btn_sign_up) {
            fragment = new SignUpFragment();
        } else if (viewId == R.id.google_login_button) {
            Intent intent = AccountPicker.zza(null, null, new String[]{"com.google"}, false, null, null, null, null, false, 1, 0);
            startActivityForResult(intent, GOOGLE_SIGN_IN);
        }

        if (fragment != null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.replaceContent(fragment, true,
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
    }


    private void socialSignIn(final String accessToken, final String provider) {
            new Thread() {
            @Override
            public void run() {
                try {
                    final User user = User.socialSignIn(accessToken, provider);
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
                    BaseActivity activity = (BaseActivity) getActivity();
                    final View view = getView();
                    if (activity != null && view != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    LoginManager.getInstance().logOut();
                                } catch (Exception e) {
                                }
                                if (e instanceof NoConnectionError) {
                                    Snackbar.make(view, R.string.no_connection, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.retry, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    socialSignIn(accessToken, provider);
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
            }
        }.start();
    }

    // Facebook callbacks
    @Override
    public void onSuccess(LoginResult loginResult) {
        socialSignIn(loginResult.getAccessToken().getToken(), "Facebook");
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(FacebookException e) {
        Snackbar.make(getView(), R.string.no_connection, Snackbar.LENGTH_LONG)
                .setDuration(Snackbar.LENGTH_LONG)
                .show();
    }
    // end Facebook callbacks

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            if (data != null) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                new RetrieveTokenTask().execute(accountName);
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getContext(), accountName, scopes);
            } catch (IOException e) {
                Logger.d("Google Auth", e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), GOOGLE_SIGN_IN);
            } catch (GoogleAuthException e) {
                Log.d("Google Auth", e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            socialSignIn(s, "Google");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.no_connection, Snackbar.LENGTH_LONG).show();
        }
    }
}