package kz.telecom.happydrive.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.CustomMessages;
import kz.telecom.happydrive.data.ResponseCode;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.proxy.AuthBackendRequests;
import kz.telecom.happydrive.proxy.ResponseCallback;
import org.json.JSONObject;

public class RegisterActivity extends ActionBarActivity implements View.OnClickListener {

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        ((Button) findViewById(R.id.bBack)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.bRegister)).setOnClickListener(this);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegister:
                User user = new User();
                user.setEmail(etEmail.getText().toString());
                user.setPassword(etPassword.getText().toString());
                new AuthBackendRequests(this).registerUserInBackground(user, new ResponseCallBackHandler(this));
                break;
//            case R.id.bBack:
//                finish();
//                break;
            default:
                break;
        }
    }

    class ResponseCallBackHandler implements ResponseCallback {

        RegisterActivity outer;

        public ResponseCallBackHandler(RegisterActivity outer) {
            this.outer = outer;
        }

        @Override
        public void done(JSONObject obj) {
            if (obj == null || obj.length() == 0) {
                showErrorMessage(CustomMessages.SERVER_UNREACHABLE);
            } else {
                switch (obj.optInt("code", -1)) {
                    case ResponseCode.OK:
                        startActivity(new Intent(outer, EmailLoginActivity.class));
                        break;
                    case ResponseCode.EMAIL_USED:
                        showErrorMessage(CustomMessages.EMAIL_USED);
                        break;
                    default:
                        showErrorMessage(CustomMessages.INVALID_FORM);
                        break;
                }
            }

        }
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

}
