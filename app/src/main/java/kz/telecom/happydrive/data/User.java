package kz.telecom.happydrive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fasterxml.jackson.databind.JsonNode;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.telecom.happydrive.data.network.JsonRequest;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
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

    public final List<FolderObject> privateFolders;

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
        card.setVisible(other.isVisible());
        card.setPayedStatus(other.isPayedStatus());
        card.setExpirationDate(other.getExpirationDate());
        card.setDomain(other.getDomain());

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

    public static User migrate(final String oldLogin, final String oldPassword, final String newEmail,
                               final String newPassword) throws ApiResponseError, NoConnectionError {
        JsonRequest request = new JsonRequest(Request.Method.POST, "auth/updateOldUser/");
        request.setBody(new Request.StringBody.Builder()
                .add("login", oldLogin)
                .add("old_password", oldPassword)
                .add("email", newEmail)
                .add("new_password", newPassword)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
            return signIn(newEmail, newPassword);
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
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
                        rawData.put(UserHelper.PREFS_KEY_PUBLIC_PHOTO_FOLDER_ID, folder.id);
                    } else if ("Видеозаписи".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PUBLIC_VIDEO_FOLDER_ID, folder.id);
                    }
                }
            }
        }

        mapOfFolders = ApiClient.getFiles(-1, false, token);
        List<ApiObject> privateFolders = mapOfFolders.get(ApiClient.API_KEY_FOLDERS);
        if (privateFolders != null) {
            for (ApiObject obj : privateFolders) {
                if (obj instanceof FolderObject) {
                    FolderObject folder = (FolderObject) obj;
                    if ("фото".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID, folder.id);
                    } else if ("видео".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID, folder.id);
                    } else if ("музыка".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID, folder.id);
                    } else if ("документы".equalsIgnoreCase(folder.name)) {
                        rawData.put(UserHelper.PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID, folder.id);
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
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
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

        List<FolderObject> privateFolders = new ArrayList<>(4);
        final int photoFolderId = Utils.getValue(Integer.class,
                UserHelper.PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID, -1, rawData);
        if (photoFolderId > 0) {
            privateFolders.add(new FolderObject(photoFolderId, "ФОТО", false, 0));
        }

        final int videoFolderId = Utils.getValue(Integer.class,
                UserHelper.PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID, -1, rawData);
        if (videoFolderId > 0) {
            privateFolders.add(new FolderObject(videoFolderId, "ВИДЕО", false, 0));
        }

        final int musicFolderId = Utils.getValue(Integer.class,
                UserHelper.PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID, -1, rawData);
        if (musicFolderId > 0) {
            privateFolders.add(new FolderObject(musicFolderId, "МУЗЫКА", false, 0));
        }

        final int documentFolderId = Utils.getValue(Integer.class,
                UserHelper.PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID, -1, rawData);
        if (documentFolderId > 0) {
            privateFolders.add(new FolderObject(documentFolderId, "ДОКУМЕНТЫ", false, 0));
        }

        Map<String, Object> cardData = (Map<String, Object>) rawData.get("card");
        if (rawData.containsKey(Card.API_KEY_VISIBILITY)) {
            cardData.put(Card.API_KEY_VISIBILITY, rawData.get(Card.API_KEY_VISIBILITY));
        }
        if (rawData.containsKey(Card.API_KEY_PAYED_STATUS)) {
            cardData.put(Card.API_KEY_PAYED_STATUS, rawData.get(Card.API_KEY_PAYED_STATUS));
        }
        if (rawData.containsKey(Card.API_KEY_EXPIRATION_DATE)) {
            cardData.put(Card.API_KEY_EXPIRATION_DATE, rawData.get(Card.API_KEY_EXPIRATION_DATE));
        }


        Card card = new Card(cardData, null);
        User user = new User(token, card, privateFolders);
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

    private User(String token, Card card, List<FolderObject> privateFolders) {
        this.token = token;
        this.card = card;

        this.privateFolders = privateFolders;
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
}