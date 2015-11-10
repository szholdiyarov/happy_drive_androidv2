package kz.telecom.happydrive.proxy;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.squareup.okhttp.*;
import kz.telecom.happydrive.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by darkhan on 07.11.15.
 */
public class AuthBackendRequests {

    private static final String API_PREFIX = "http://hd.todo.kz";
    private static final String GET_TOKEN = API_PREFIX + "/auth/getToken/";
    private static final String GET_SOCIAL_TOKEN = API_PREFIX + "/auth/getSocialToken/";
    private static final String AUTH_REGISTER = API_PREFIX + "/auth/register/";
    private static final String AUTH_RESET = API_PREFIX + "/auth/reset/";

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
        Map<String, String> postParams = new HashMap<>();
        postParams.put("email", user.getEmail());
        postParams.put("password", user.getPassword());
        new PostAsyncTask(postParams, GET_TOKEN, callback).execute();
    }

    public void loginUserInBackground(String accessToken, String provider, ResponseCallback callback) {
        progressDialog.show();
        Map<String, String> postParams = new HashMap<>();
        postParams.put("access_token", accessToken);
        postParams.put("provider", provider);
        new PostAsyncTask(postParams, GET_SOCIAL_TOKEN, callback).execute();
    }


    public void resetPasswordInBackground(String email, ResponseCallback callback) {
        progressDialog.show();
        Map<String, String> postParams = new HashMap<>();
        postParams.put("email", email);
        new PostAsyncTask(postParams, AUTH_RESET, callback).execute();
    }

    public void registerUserInBackground(User user, ResponseCallback callback) {
        progressDialog.show();
        Map<String, String> postParams = new HashMap<>();
        postParams.put("email", user.getEmail());
        postParams.put("password", user.getPassword());
        new PostAsyncTask(postParams, AUTH_REGISTER, callback).execute();
    }

    private class PostAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        /**
         * General class for making POST requests to server. Since all server response is in json format,
         * AsyncTask will always return JSONObject (maybe null or empty if request was unsuccessful).
         */

        private Map<String, String> postParams;
        private String fullUrl;
        private ResponseCallback callback;

        public PostAsyncTask(Map<String, String> postParams, String fullUrl, ResponseCallback callback) {
            this.postParams = postParams;
            this.fullUrl = fullUrl;
            this.callback = callback;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            // Prepare POST form
            FormEncodingBuilder builder = new FormEncodingBuilder();
            for (Map.Entry<String, String> param : postParams.entrySet()) {
                builder.add(param.getKey(), param.getValue());
            }
            RequestBody body = builder.build();
            Request request = new Request.Builder()
                                            .url(fullUrl)
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
