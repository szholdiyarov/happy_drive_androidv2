package kz.telecom.happydrive.proxy;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.squareup.okhttp.*;
import kz.telecom.happydrive.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by darkhan on 07.11.15.
 */
public class AuthBackendRequests {

    private static final String API_PREFIX = "http://hd.todo.kz";
    private static final String GET_TOKEN = API_PREFIX + "/auth/getToken/";
    private static final String AUTH_REGISTER = API_PREFIX + "/auth/register/";

    private static final String PREF_KEY_TOKEN = "token";
    private static final int CONNECTION_TIMEOUT = 1000 * 15;

    private ProgressDialog progressDialog;

    private OkHttpClient client;

    public AuthBackendRequests(Context context) {
        client = new OkHttpClient();
        client.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Идет обработка...");
        progressDialog.setMessage("Подождите пожалуйста...");
    }

    public void loginUserInBackground(User user, ResponseCallback callback) {
        progressDialog.show();
        new LoginUserAsyncTask(user, callback).execute();
    }

    public void registerUserInBackground(User user, ResponseCallback callback) {
        progressDialog.show();
        new RegisterUserAsyncTask(user, callback).execute();
    }

    private class RegisterUserAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private User user;
        private ResponseCallback callback;

        public RegisterUserAsyncTask(User user, ResponseCallback callback) {
            this.user = user;
            this.callback = callback;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            RequestBody body = new FormEncodingBuilder()
                                        .add("email", user.getEmail())
                                        .add("password", user.getPassword())
                                        .build();
            Request request = new Request.Builder()
                                             .url(AUTH_REGISTER)
                                             .post(body)
                                             .build();

            JSONObject obj = null;
            try {
                Response response = client.newCall(request).execute();
                obj = new JSONObject(response.body().string());
            } catch (IOException e) {
                // TODO: Logging
                obj = null;
            } catch (JSONException e) {
                // TODO: Logging
                obj = null;
            }
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            progressDialog.dismiss();
            callback.done(obj);
            super.onPostExecute(obj);
        }
    }
    private class LoginUserAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private User user;
        private ResponseCallback callback;

        public LoginUserAsyncTask(User user, ResponseCallback callback) {
            this.user = user;
            this.callback = callback;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            RequestBody body = new FormEncodingBuilder()
                    .add("email", user.getEmail())
                    .add("password", user.getPassword())
                    .build();
            Request request = new Request.Builder()
                    .url(GET_TOKEN)
                    .post(body)
                    .build();

            JSONObject obj = null;
            try {
                Response response = client.newCall(request).execute();
                obj = new JSONObject(response.body().string());
            } catch (IOException e) {
                // TODO: Logging
                obj = null;
            } catch (JSONException e) {
                // TODO: Logging
                obj = null;
            }
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            progressDialog.dismiss();
            callback.done(obj);
            super.onPostExecute(obj);
        }
    }

}
