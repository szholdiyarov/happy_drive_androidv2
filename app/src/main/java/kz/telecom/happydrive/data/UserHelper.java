package kz.telecom.happydrive.data;

import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import java.util.HashMap;
import java.util.Map;

import kz.telecom.happydrive.data.network.NetworkManager;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
class UserHelper {
    private static final String API_PATH_GET_TOKEN = "/auth/getToken/";
    private static final String API_PATH_REGISTER = "/auth/register/";
    private static final String API_PATH_RESET_PASSWORD = "/auth/reset/";

    static final String API_USER_KEY_EMAIL = "email";
    static final String API_USER_KEY_PASSWORD = "password";
    static final String API_USER_KEY_TOKEN = "token";
    static final String API_USER_KEY_CARD = "card";
    static final String API_USER_KEY_CARD_ID = "card_id";
    static final String API_USER_VISIBLE = "visible";

    static JsonNode register(final String email, final String password) throws Exception {
        RequestBody requestBody = new FormEncodingBuilder()
                .add(API_USER_KEY_EMAIL, email)
                .add(API_USER_KEY_PASSWORD, password)
                .build();

        final String response = NetworkManager.post(API_PATH_REGISTER, requestBody);
        return new ObjectMapper().readTree(response);
    }

    static JsonNode getToken(final String email, final String password) throws Exception {
        RequestBody requestBody = new FormEncodingBuilder()
                .add(API_USER_KEY_EMAIL, email)
                .add(API_USER_KEY_PASSWORD, password)
                .build();

        final String response = NetworkManager.post(API_PATH_GET_TOKEN, requestBody);
        return new ObjectMapper().readTree(response);
    }

    static JsonNode resetPassword(final String email) throws Exception {
        RequestBody requestBody = new FormEncodingBuilder()
                .add(API_USER_KEY_EMAIL, email).build();

        final String response = NetworkManager.post(API_PATH_RESET_PASSWORD, requestBody);
        return new ObjectMapper().readTree(response);
    }

    static void saveCredentials(User user, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_USER_KEY_EMAIL, user.email);
        editor.putString(API_USER_KEY_TOKEN, user.token);
//        editor.putString(API_USER_KEY_CARD_ID, user.cardId);
        editor.apply();
    }

    static void wipeCredentials(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();
    }

    static Map<String, Object> restoreFromCredentials(SharedPreferences prefs) {
        if (!prefs.contains(API_USER_KEY_TOKEN)) {
            return null;
        }

        Map<String, Object> raw = new HashMap<>(2);
        raw.put(API_USER_KEY_EMAIL, prefs.getString(API_USER_KEY_EMAIL, null));
        raw.put(API_USER_KEY_TOKEN, prefs.getString(API_USER_KEY_TOKEN, null));

        return raw;
    }

    static <T> T getValue(Class<?> cls, String key, T fallback, Map<String, Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            return fallback;
        }

        if (cls.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }

        return fallback;
    }
}
