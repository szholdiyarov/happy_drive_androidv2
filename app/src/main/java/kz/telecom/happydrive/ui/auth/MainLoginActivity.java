package kz.telecom.happydrive.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
//import com.facebook.*;
//import com.facebook.appevents.AppEventsLogger;
//import com.facebook.login.LoginResult;
//import com.facebook.login.widget.LoginButton;
//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.auth.api.signin.GoogleSignInResult;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.CustomMessages;
import kz.telecom.happydrive.data.ResponseCode;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.UserLocalStore;
import kz.telecom.happydrive.proxy.AuthBackendRequests;
import kz.telecom.happydrive.proxy.ResponseCallback;
import kz.telecom.happydrive.ui.MainActivity;
import org.json.JSONObject;

import java.util.Arrays;


public class MainLoginActivity extends ActionBarActivity implements View.OnClickListener
//                                                        GoogleApiClient.OnConnectionFailedListener
{

    private ImageButton bLogin, bRegister;
    private UserLocalStore userLocalStore;
//    private LoginButton fbLogin;
//    private CallbackManager callbackManager;
    private MainLoginActivity outer;
//    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_SIGN_IN = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize Facebook sdk before setContentView
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                                                               .requestEmail()
//                                                               .requestIdToken(getString(R.string.google_server_client_id))
//                                                               .build();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

        setContentView(R.layout.main_activity_login);
        outer = this;

        // check if user is already logged in.
        userLocalStore = new UserLocalStore(this);
        if (userLocalStore.getToken() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        bLogin = (ImageButton) findViewById(R.id.bLogin);
        bRegister = (ImageButton) findViewById(R.id.bRegister);

        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);

        // ----- GOOGLE PLUS LOGIN -----
//        findViewById(R.id.google_login_button).setOnClickListener(this);

        // ----- END GOOGLE PLUS LOGIN

        // ----- FACEBOOK LOGIN BUTTON AND CALLBACK -----
//        callbackManager = CallbackManager.Factory.create();
//        fbLogin = (LoginButton)findViewById(R.id.login_button);
//        fbLogin.setReadPermissions(Arrays.asList("email"));
//
//        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                new AuthBackendRequests(outer).loginUserInBackground(loginResult.getAccessToken().getToken(), "Facebook",
//                        new ResponseCallback() {
//                            @Override
//                            public void done(JSONObject obj) {
//                                if (obj == null || obj.length() == 0) {
//                                    showErrorMessage(CustomMessages.SERVER_UNREACHABLE);
//                                } else {
//                                    switch (obj.optInt("code", -1)) {
//                                        case ResponseCode.OK:
//                                            JSONObject card = obj.optJSONObject("card");
//                                            User user = new User();
//                                            user.setEmail(card.optString("email", "unknown"));
//                                            user.setToken(obj.optString("token"));
//                                            logUserIn(user);
//                                            break;
//                                        default:
//                                            showErrorMessage(CustomMessages.INCORRECT_EMAIL_OR_PASSWORD);
//                                            break;
//                                    }
//                                }
//                            }
//                        }
//                );
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//
//            @Override
//            public void onError(FacebookException e) {
//
//            }
//
//        });
        // ----- END FACEBOOK LOGIN -----


    }


    private void logUserIn(User user) {
        userLocalStore.storeUserData(user);
        startActivity(new Intent(this, MainActivity.class));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.google_login_button:
//                Log.d("Google+", "login");
//                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
//                break;
            case R.id.bLogin:
                startActivity(new Intent(this, EmailLoginActivity.class));
                break;
            case R.id.bRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == GOOGLE_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleSignInResult(result);
//        } else {
//            callbackManager.onActivityResult(requestCode, resultCode, data);
//        }
    }
//
//    private void handleSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            // Signed in successfully, show authenticated UI.
//            GoogleSignInAccount acct = result.getSignInAccount();
//            String accessToken = acct.getIdToken();
//        } else {
//            showErrorMessage(CustomMessages.INCORRECT_EMAIL_OR_PASSWORD);
//        }
//    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainLoginActivity.this);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        showErrorMessage(CustomMessages.CONNECTION_FAILED);
//    }
}
