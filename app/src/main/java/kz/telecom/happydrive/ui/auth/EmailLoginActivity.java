package kz.telecom.happydrive.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.CustomMessages;
import kz.telecom.happydrive.data.ResponseCode;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.UserLocalStore;
import kz.telecom.happydrive.proxy.AuthBackendRequests;
import kz.telecom.happydrive.proxy.ResponseCallback;
import kz.telecom.happydrive.ui.MainActivity;
import org.json.JSONObject;


public class EmailLoginActivity extends ActionBarActivity implements View.OnClickListener {

    private ImageButton bLogin;
    private EditText etUsername, etPassword;
    private UserLocalStore userLocalStore;
    private Button bRegister, bForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_activity_login);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bRegister = (Button) findViewById(R.id.bRegister);
        bRegister.setOnClickListener(this);
        bForgotPassword = (Button) findViewById(R.id.bForgotPassword);
        bForgotPassword.setOnClickListener(this);
        bLogin = (ImageButton) findViewById(R.id.bLogin);
        bLogin.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.bForgotPassword:
                startActivity(new Intent(this, ResetActivity.class));
                break;
            case R.id.bLogin:
                User user = new User();
                user.setEmail(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                authenticate(user);
                break;
            default:
                break;
        }
    }

    private void authenticate(final User user) {
        new AuthBackendRequests(this).loginUserInBackground(user, new ResponseCallback() {
            @Override
            public void done(JSONObject obj) {
                if (obj == null || obj.length() == 0) {
                    showErrorMessage(CustomMessages.SERVER_UNREACHABLE);
                } else {
                    switch (obj.optInt("code", -1)) {
                        case ResponseCode.OK:
                            // TODO: Store more data.
                            user.setCardId(obj.optString("token"));
                            logUserIn(user);
                            break;
                        default:
                            etPassword.setError(CustomMessages.INCORRECT_EMAIL_OR_PASSWORD);
                            break;
                    }
                }
            }
        });

    }

    private void logUserIn(User user) {
        userLocalStore.storeUserData(user);
        startActivity(new Intent(this, MainActivity.class));
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EmailLoginActivity.this);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

}
