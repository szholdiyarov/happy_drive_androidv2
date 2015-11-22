package kz.telecom.happydrive.data;

import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import kz.telecom.happydrive.data.network.JsonRequest;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.ResponseParseError;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
class UserHelper {
    private static final String API_PATH_GET_TOKEN = "auth/getToken/";
    private static final String API_PATH_REGISTER = "auth/register/";
    private static final String API_PATH_RESET_PASSWORD = "auth/reset/";

    static final String API_USER_KEY_EMAIL = "email";
    static final String API_USER_KEY_PASSWORD = "password";
    static final String API_USER_KEY_TOKEN = "token";
    static final String API_USER_KEY_CARD = "card";
    static final String API_USER_VISIBLE = "visible";

    static JsonNode register(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_REGISTER);
        request.setBody(new Request.StringBody.Builder()
                .add(API_USER_KEY_EMAIL, email)
                .add(API_USER_KEY_PASSWORD, password)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkAndThrowIfNeeded(response);
            return response.result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static JsonNode getToken(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_GET_TOKEN);
        request.setBody(new Request.StringBody.Builder()
                .add(API_USER_KEY_EMAIL, email)
                .add(API_USER_KEY_PASSWORD, password)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkAndThrowIfNeeded(response);
            return response.result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static JsonNode resetPassword(final String email)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_RESET_PASSWORD);
        request.setBody(new Request.StringBody.Builder()
                .add(API_USER_KEY_EMAIL, email).build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkAndThrowIfNeeded(response);
            return response.result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static void saveCredentials(User user, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_USER_KEY_TOKEN, user.token);
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
        raw.put(API_USER_KEY_TOKEN, prefs.getString(API_USER_KEY_TOKEN, null));
        raw.put(API_USER_KEY_CARD, Card.restoreUserCard(prefs));

        return raw;
    }

    private static void checkAndThrowIfNeeded(Response<JsonNode> response)
            throws ResponseParseError, ApiResponseError {
        JsonNode jsonNode = response.result;
        if (response.isSuccessful() && jsonNode != null) {
            int responseCode = ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR;
            if (jsonNode.hasNonNull(ApiResponseError.API_RESPONSE_CODE_KEY)) {
                responseCode = jsonNode.get(ApiResponseError.API_RESPONSE_CODE_KEY)
                        .asInt(ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR);
            }

            if (responseCode != ApiResponseError.API_RESPONSE_CODE_OK) {
                throw new ApiResponseError("api response error", responseCode, null);
            }
        } else if (response.exception != null) {
            throw new ResponseParseError("response parse error", response.exception);
        }
    }
}
