package kz.telecom.happydrive.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.data.UserLocalStore;
import kz.telecom.happydrive.ui.MainActivity;

public class MainLoginActivity extends ActionBarActivity implements View.OnClickListener {

    private ImageButton bLogin, bRegister;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_login);

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
}
