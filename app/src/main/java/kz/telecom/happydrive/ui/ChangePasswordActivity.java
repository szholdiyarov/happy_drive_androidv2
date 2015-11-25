package kz.telecom.happydrive.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import kz.telecom.happydrive.R;
import kz.telecom.happydrive.ui.fragment.AuthFragment;

/**
 * Created by darkhan on 25.11.15.
 */
public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {


    private EditText etOldPassword;
    private EditText etPassword2;
    private EditText etPassword1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.fragment_change_pwd);

        etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        etPassword1 = (EditText) findViewById(R.id.etPassword1);
        etPassword2 = (EditText) findViewById(R.id.etPassword2);
        findViewById(R.id.bChangePassword).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Snackbar.make(v, R.string.no_connection, Snackbar.LENGTH_LONG).show();
    }
}
