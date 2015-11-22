package kz.telecom.happydrive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.ResponseParseError;
import kz.telecom.happydrive.util.Utils;


// TODO more secure way to store passwords (AccountManager)
// TODO thread safe consistent simultaneous currentUser() calls
public class User {
    @SuppressWarnings("unused")
    private static final String PREFS_NAME = "hd.user";

    private static User sUser;
    @NonNull
    public final Card card;
    @NonNull
    final String token;


    public static boolean isAuthenticated() {
        return currentUser() != null;
    }

    public static User currentUser() {
        Map<String, Object> rawData;
        if (sUser == null && (rawData = UserHelper.restoreFromCredentials(getDefaultSharedPrefs())) != null) {
            try {
                initStaticUser(parseUser(rawData));
            } catch (ResponseParseError ignored) {
            }
        }

        return sUser;
    }

    @NonNull
    @WorkerThread
    public static User signIn(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonNode jsonNode = UserHelper.getToken(email, password);
        Map<String, Object> rawData = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .convertValue(jsonNode, Map.class);

        User user = parseUser(rawData);
        SharedPreferences prefs = getDefaultSharedPrefs();
        UserHelper.saveCredentials(user, prefs);
        Card.saveUserCard(user.card, prefs);
        return initStaticUser(user);
    }

    @NonNull
    @WorkerThread
    public static User signUp(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        UserHelper.register(email, password);
        return signIn(email, password);
    }

    @WorkerThread
    public static void restorePassword(final String email)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        UserHelper.resetPassword(email);
    }

    private static SharedPreferences getDefaultSharedPrefs() {
        Context context = DataManager.getInstance().context;
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static User parseUser(Map<String, Object> rawData) throws ResponseParseError {
        String token = Utils.getValue(String.class, UserHelper.API_USER_KEY_TOKEN, null, rawData);
        if (token == null) {
            throw new ResponseParseError("token is null");
        }

        Card card = new Card((Map<String, Object>) rawData.get("card"));
        return new User(token, card);
    }

    protected static User initStaticUser(User user) {
        try {
            NetworkManager.setCookie(Request.DEFAULT_HOST, "auth-token", user.token);
        } catch (URISyntaxException | IOException ignored) {
        }

        return sUser = user;
    }

    protected static void deinitStaticUser(User user) {
        try {
            NetworkManager.removeCookie(Request.DEFAULT_HOST);
        } catch (URISyntaxException | IOException ignored) {
        }

        sUser = null;
    }

    private User(String token, Card card) {
        this.token = token;
        this.card = card;
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