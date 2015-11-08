package kz.telecom.happydrive.ui.auth;

import android.app.AlertDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.CustomMessages;
import kz.telecom.happydrive.data.ResponseCode;
import kz.telecom.happydrive.data.UserLocalStore;
import kz.telecom.happydrive.proxy.AuthBackendRequests;
import kz.telecom.happydrive.proxy.ResponseCallback;
import org.json.JSONObject;

public class ResetActivity extends ActionBarActivity implements View.OnClickListener {

    private UserLocalStore userLocalStore;
    private ImageButton ibSend;
    private EditText etEmail;
    private ResetActivity outer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        etEmail = (EditText) findViewById(R.id.etEmail);
        ibSend = (ImageButton) findViewById(R.id.ibSend);
        ibSend.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);

        outer = this;
    }


    @Override
    public void onClick(View v) {
        // Clear data about user from sharedPreferences
        userLocalStore.logout();
        new AuthBackendRequests(this).resetPasswordInBackground(etEmail.getText().toString(), new ResponseCallback() {

            @Override
            public void done(JSONObject obj) {
                if (obj == null || obj.length() == 0) {
                    showErrorMessage(CustomMessages.SERVER_UNREACHABLE);
                } else {
                    Log.i(Integer.toString(obj.optInt("code")), " <-- code");
                    switch (obj.optInt("code", -1)) {
                        case ResponseCode.OK:
                            showErrorMessage(CustomMessages.PASSWORD_RESET_EMAIL_SENT);
                            break;
                        default:
                            etEmail.setError(CustomMessages.EMAIL_NOT_FOUND);
                            break;
                    }
                }
            }

        });

    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ResetActivity.this);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.show();
    }

}
