package kz.telecom.happydrive.ui.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
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
import kz.telecom.happydrive.ui.BaseActivity;

/**
 * Created by darkhan on 26.11.15.
 */
public class ChangePasswordFragment extends BaseFragment implements View.OnClickListener{

    private EditText etOldPassword;
    private EditText etPassword2;
    private EditText etPassword1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_pwd, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.layout_toolbar_auth);
        ActionBar actionBar = activity.initToolbar(toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Сменить пароль");

        etOldPassword = (EditText) view.findViewById(R.id.etOldPassword);
        etPassword1 = (EditText) view.findViewById(R.id.etPassword1);
        etPassword2 = (EditText) view.findViewById(R.id.etPassword2);
        view.findViewById(R.id.bChangePassword).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = false;
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        final String oldPwd = etOldPassword.getText().toString();
        final String newPwd1 = etPassword1.getText().toString();
        String newPwd2 = etPassword2.getText().toString();
        if (newPwd1.length() == 0) {
            etPassword1.setError("Пароль не может быть пустым");
        } else if (!newPwd1.equals(newPwd2)) {
            etPassword2.setError("Пароли не совпадают");
        } else {
            new Thread() {
                @Override
                public void run() {
                    final boolean result = ApiClient.changePassword(oldPwd, newPwd1);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                // Dummy clear
                                etOldPassword.setText("");
                                etPassword1.setText("");
                                etPassword2.setText("");
                                Snackbar.make(v, "Пароль успешно изменен", Snackbar.LENGTH_LONG).show();
                            } else {
                                etOldPassword.setError("Текущий пароль не верный");
                            }
                        }
                    });
                }


            }.start();
        }
    }

}
