package kz.telecom.happydrive.proxy;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.squareup.okhttp.*;
import kz.telecom.happydrive.data.ResponseCode;
import kz.telecom.happydrive.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BackendRequests {

    private static final String API_PREFIX = "http://hd.todo.kz";
    private static final String GET_TOKEN = API_PREFIX + "/auth/getToken/";

    private static final String PREF_KEY_TOKEN = "token";
    private static final int CONNECTION_TIMEOUT = 1000 * 15;

    private ProgressDialog progressDialog;

    private OkHttpClient client;

    public BackendRequests(Context context) {
        client = new OkHttpClient();
        client.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing...");
        progressDialog.setMessage("Please wait...");
    }

    public void loginUserInBackground(User user, ResponseCallback callback) {
        progressDialog.show();
        new LoginUserAsyncTask(user, callback).execute();
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
                                        .add("email", user.getUsername())
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

        private void handleResponse(JSONObject obj) {
            if (obj == null || obj.length() == 0) {
                // in-proper response

            } else {
                switch (obj.optInt("code")) {
                    case ResponseCode.OK:
                        // auth in OK. save user info to sharedPreferences
                        break;
                    default:
                        // show error message
                        break;
                }
            }
        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            progressDialog.dismiss();
            callback.done(obj);
            super.onPostExecute(obj);
        }
    }

}
