package kz.telecom.happydrive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.ResponseParseError;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;


// TODO more secure way to store passwords (AccountManager)
// TODO thread safe consistent simultaneous currentUser() calls
public class User {
    @SuppressWarnings("unused")
    private static final String PREFS_NAME = "hd.user";
    public static final String API_KEY_CURRENT_PASSWORD = "current_password";
    public static final String API_KEY_NEW_PASSWORD = "new_password";

    private static User sUser;
    @NonNull
    public final Card card;
    @NonNull
    public final String token;
    private long mStorageUsed;
    private long mStorageTotal;

    @WorkerThread
    public boolean updateCard() throws NoConnectionError, ApiResponseError, ResponseParseError {
        Card other = ApiClient.getCard(card.id);
        card.setCategoryId(other.getCategoryId());
        card.setFirstName(other.getFirstName());
        card.setLastName(other.getLastName());
        card.setPhone(other.getPhone());
        card.setEmail(other.getEmail());
        card.setAddress(other.getAddress());
        card.setWorkPlace(other.getWorkPlace());
        card.setPosition(other.getPosition());
        card.setShortDesc(other.getShortDesc());
        card.setFullDesc(other.getFullDesc());
        card.setAudio(other.getAudio());
        card.setAvatar(other.getAvatar());
        card.setBackground(other.getBackground());
        card.visible = other.visible;

        card.publicFolders.clear();
        card.publicFolders.addAll(other.publicFolders);

        Card.saveUserCard(card, getDefaultSharedPrefs());

        return true;
    }

    @WorkerThread
    public synchronized boolean changeAudio(File file) throws NoConnectionError,
            ApiResponseError, ResponseParseError {
        JsonNode jsonNode = UserHelper.changeAudio(file);
        if (jsonNode.hasNonNull("url")) {
            card.setAudio(jsonNode.get("url").asText());
            Card.saveUserCard(card, getDefaultSharedPrefs());
            return true;
        }

        return false;
    }

    @WorkerThread
    public synchronized boolean changeAvatar(File file) throws NoConnectionError,
            ApiResponseError, ResponseParseError {
        JsonNode jsonNode = UserHelper.changeAvatar(file);
        if (jsonNode.hasNonNull("url")) {
            card.setAvatar(jsonNode.get("url").asText());
            Card.saveUserCard(card, getDefaultSharedPrefs());
            return true;
        }

        return false;
    }

    @WorkerThread
    public synchronized boolean changeBackground(File file) throws NoConnectionError,
            ApiResponseError, ResponseParseError {
        JsonNode jsonNode = UserHelper.changeBackground(file);
        if (jsonNode.hasNonNull("url")) {
            card.setBackground(jsonNode.get("url").asText());
            Card.saveUserCard(card, getDefaultSharedPrefs());
            return true;
        }

        return false;
    }

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

    public static User socialSignIn(final String accessToken, final String provider) throws Exception {
        return signIn(accessToken, provider, true);
    }

    @NonNull
    @WorkerThread
    public static User signIn(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        return signIn(email, password, false);
    }

    private static User signIn(final String arg1, final String arg2, boolean isSocial)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonNode jsonNode = isSocial ? UserHelper.getSocialToken(arg1, arg2) :
                UserHelper.getToken(arg1, arg2);
        Map<String, Object> rawData = ApiClient.getObjectMapper()
                .convertValue(jsonNode, Map.class);

        String token = Utils.getValue(String.class, UserHelper.API_USER_KEY_TOKEN, null, rawData);
        Map<String, List<ApiObject>> mapOfFolders = ApiClient.getFiles(-1, true, token);
        List<ApiObject> publicFolders = mapOfFolders.get(ApiClient.API_KEY_FOLDERS);
        if (publicFolders != null) {
            for (ApiObject obj : publicFolders) {
                if (obj instanceof FolderObject) {
                    FolderObject folder = (FolderObject) obj;
                    if ("фотографии".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PHOTO_FOLDER_ID, folder.id);
                    } else if ("Видеозаписи".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_VIDEO_FOLDER_ID, folder.id);
                    }
                }
            }
        }

        long[] storageSize = UserHelper.getStorageSize(token);
        rawData.put(UserHelper.PREFS_KEY_STORAGE_USED, storageSize[0]);
        rawData.put(UserHelper.PREFS_KEY_STORAGE_TOTAL, storageSize[1]);

        User user = parseUser(rawData);
        SharedPreferences prefs = getDefaultSharedPrefs();
        UserHelper.saveCredentials(user, prefs);
        Card.saveUserCard(user.card, prefs);
        ParseInstallation.getCurrentInstallation()
                .put("email", isSocial ? rawData.get("login") : arg1);
        ParseInstallation.getCurrentInstallation().saveInBackground();
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

    @MainThread
    public void signOut() {
        UserHelper.wipeCredentials(getDefaultSharedPrefs());
        deinitStaticUser(sUser);
    }

    public long getStorageUsed() {
        return mStorageUsed;
    }

    public long getStorageTotal() {
        return mStorageTotal;
    }

    @WorkerThread
    public void updateStorageSize() throws NoConnectionError, ApiResponseError, ResponseParseError {
        long[] storageSize = UserHelper.getStorageSize(null);
        mStorageUsed = storageSize[0];
        mStorageTotal = storageSize[1];
        UserHelper.saveCredentials(this, getDefaultSharedPrefs());
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

        Card card = new Card((Map<String, Object>) rawData.get("card"),
                (List<Map<String, Object>>) rawData.get("folders"));
        User user = new User(token, card);
        user.mStorageUsed = Utils.getValue(Long.class, UserHelper.PREFS_KEY_STORAGE_USED, -1L, rawData);
        user.mStorageTotal = Utils.getValue(Long.class, UserHelper.PREFS_KEY_STORAGE_TOTAL, -1L, rawData);
        return user;
    }

    protected static User initStaticUser(User user) {
        try {
            NetworkManager.setCookie(Request.DEFAULT_HOST, "Auth-Token", user.token);
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

    public static class OnStorageSizeUpdatedEvent {
        public OnStorageSizeUpdatedEvent() {
        }
    }

    public static class OnPortfolioPhotoUploadEvent {
        public final FileObject fileObject;

        public OnPortfolioPhotoUploadEvent(FileObject fileObject) {
            this.fileObject = fileObject;
        }
    }

    public static class OnPortfolioPhotoDeletedEvent {
        public final int folderId;

        public OnPortfolioPhotoDeletedEvent(int folderId) {
            this.folderId = folderId;
        }
    }
}