package kz.telecom.happydrive.ui.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.ApiResponseError;
import kz.telecom.happydrive.data.DataManager;
import kz.telecom.happydrive.data.NoConnectionError;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.ui.AuthActivity;
import kz.telecom.happydrive.ui.BaseActivity;
import kz.telecom.happydrive.ui.MainActivity;


/**
 * Created by Galymzhan Sh on 11/15/15.
 */
public class AuthFragment extends BaseFragment implements View.OnClickListener, FacebookCallback<LoginResult>, GoogleApiClient.OnConnectionFailedListener {

    private static final int GOOGLE_SIGN_IN = 999;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                         .requestEmail()
                                                         .requestIdToken(getString(R.string.google_server_client_id))
                                                         .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                                              .enableAutoManage(getActivity(), this)
                                              .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                              .build();
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

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setFragment(this);

        loginButton.registerCallback(callbackManager, this);

        view.findViewById(R.id.google_login_button).setOnClickListener(this);

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
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
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
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                String accessToken = acct.getIdToken();
                socialSignIn(accessToken, "Google");
            } else {
                onError(null);
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(getView(), R.string.no_connection, Snackbar.LENGTH_LONG).show();
    }
}