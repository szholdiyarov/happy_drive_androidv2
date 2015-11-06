package kz.telecom.happydrive.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.User;
import kz.telecom.happydrive.data.UserLocalStore;
import kz.telecom.happydrive.proxy.BackendRequests;
import kz.telecom.happydrive.proxy.ResponseCallback;
import org.json.JSONObject;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    Button bLogin;
    EditText etUsername, etPassword;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);

        bLogin.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                User user = new User();
                user.setUsername(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                authenticate(user);
                break;
            default:
                break;
        }
    }

    private void authenticate(User user) {
        new BackendRequests(this).loginUserInBackground(user, new ResponseCallback() {
            @Override
            public void done(JSONObject obj) {
                
            }
        });

    }
}
