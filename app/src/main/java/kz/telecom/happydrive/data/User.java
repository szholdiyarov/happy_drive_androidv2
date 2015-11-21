package kz.telecom.happydrive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.ResponseParseError;


// TODO more secure way to store passwords (AccountManager)
// TODO thread safe consistent simultaneous currentUser() calls
public class User {
    @SuppressWarnings("unused")
    private static final String PREFS_NAME = "hd.user";

    private static User sUser;

    public final String email;
    final String token;


    public static boolean isAuthenticated() {
        return currentUser() != null;
    }

    public static User currentUser() {
        Map<String, Object> rawData;
        if (sUser == null && (rawData = UserHelper.restoreFromCredentials(getDefaultSharedPrefs())) != null) {
            try {
                sUser = parseUser(rawData);
            } catch (ResponseParseError ignored) {
            }
        }

        return sUser;
    }

    @NonNull
    @WorkerThread
    public static User signIn(final String email, final String password) throws Exception {
        JsonNode jsonNode;
        try {
            jsonNode = UserHelper.getToken(email, password);
        } catch (IOException ioe) {
            throw new NoConnectionError("no network error", ioe);
        }

        final int responseCode = jsonNode.hasNonNull(ApiResponseError.API_RESPONSE_CODE_KEY) ?
                jsonNode.get(ApiResponseError.API_RESPONSE_CODE_KEY)
                        .asInt(ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR) :
                ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR;

        if (responseCode != ApiResponseError.API_RESPONSE_CODE_OK) {
            throw new ApiResponseError("api response error", responseCode, null);
        }

        Map<String, Object> rawData = new ObjectMapper().convertValue(jsonNode, Map.class);
        rawData.put(UserHelper.API_USER_KEY_EMAIL, email);
        User user = parseUser(rawData);
        SharedPreferences prefs = getDefaultSharedPrefs();
        UserHelper.wipeCredentials(prefs);
        UserHelper.saveCredentials(user, prefs);
        return sUser = user;
    }

    @NonNull
    @WorkerThread
    public static User signUp(final String email, final String password) throws Exception {
        JsonNode jsonNode;
        try {
            jsonNode = UserHelper.register(email, password);
        } catch (IOException ioe) {
            throw new NoConnectionError("no network error", ioe);
        }

        final int responseCode = jsonNode.hasNonNull(ApiResponseError.API_RESPONSE_CODE_KEY) ?
                jsonNode.get(ApiResponseError.API_RESPONSE_CODE_KEY)
                        .asInt(ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR) :
                ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR;

        if (responseCode != ApiResponseError.API_RESPONSE_CODE_OK) {
            throw new ApiResponseError("api response error", responseCode, null);
        }

        return signIn(email, password);
    }

    @WorkerThread
    public static void restorePassword(final String email) throws Exception {
        JsonNode jsonNode;
        try {
            jsonNode = UserHelper.resetPassword(email);
        } catch (IOException ioe) {
            throw new NoConnectionError("no network error", ioe);
        }

        int responseCode = ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR;
        if (jsonNode.hasNonNull(ApiResponseError.API_RESPONSE_CODE_KEY)) {
            responseCode = jsonNode.get(ApiResponseError.API_RESPONSE_CODE_KEY)
                    .asInt(ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR);
        }

        if (responseCode != ApiResponseError.API_RESPONSE_CODE_OK) {
            throw new ApiResponseError("api response error", responseCode, null);
        }
    }

    private static SharedPreferences getDefaultSharedPrefs() {
        Context context = DataManager.getInstance().context;
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static User parseUser(Map<String, Object> rawData) throws ResponseParseError {
        String email = UserHelper.getValue(String.class, UserHelper.API_USER_KEY_EMAIL, null, rawData);
        String token = UserHelper.getValue(String.class, UserHelper.API_USER_KEY_TOKEN, null, rawData);
        if (email == null || token == null) {
            throw new ResponseParseError("email or token is null");
        }

        return new User(email, token, null);
    }

    private User(String email, String token, String cardId) {
        this.email = email;
        this.token = token;
    }

    public static class SignedInEvent {
        public final User user;

        public SignedInEvent(User user) {
            this.user = user;
        }
    }

    public static class SignedUpEvent {
        public final User user;

        public SignedUpEvent(User user) {
            this.user = user;
        }
    }

    public static class SignedOutEvent {
        public SignedOutEvent() {
        }
    }

    public static class PasswordRestoredEvent {
        public PasswordRestoredEvent() {
        }
    }
}