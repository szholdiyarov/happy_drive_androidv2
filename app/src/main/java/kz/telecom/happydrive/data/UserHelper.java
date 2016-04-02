package kz.telecom.happydrive.data;

import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
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
    private static final String API_PATH_GET_SOCIAL_TOKEN = "auth/getSocialToken/";
    private static final String API_PATH_GET_TOKEN = "auth/getToken/";
    private static final String API_PATH_REGISTER = "auth/register/";
    private static final String API_PATH_RESET_PASSWORD = "auth/reset/";
    private static final String API_PATH_UPDATE_AUDIO = "card/audio/";
    private static final String API_PATH_UPDATE_AVATAR = "card/avatar/";
    private static final String API_PATH_UPDATE_BACKGROUND = "card/background/";
    private static final String API_PATH_STORAGE_SIZE = "files/storage_size/";

    static final String API_USER_KEY_EMAIL = "email";
    static final String API_USER_KEY_PASSWORD = "password";
    static final String API_USER_KEY_TOKEN = "token";
    static final String API_USER_KEY_SOCIAL_TOKEN = "access_token";
    static final String API_USER_KEY_SOCIAL_PROVIDER = "provider";
    static final String API_USER_KEY_CARD = "card";
    static final String API_USER_VISIBLE = "visible";

    static final String PREFS_KEY_PUBLIC_PHOTO_FOLDER_ID = "hd.user.public.photoFolderId";
    static final String PREFS_KEY_PUBLIC_VIDEO_FOLDER_ID = "hd.user.public.videoFolderId";
    static final String PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID = "hd.user.private.photoFolderId";
    static final String PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID = "hd.user.private.videoFolderId";
    static final String PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID = "hd.user.private.musicFolderId";
    static final String PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID = "hd.user.private.documentFolderId";
    static final String PREFS_KEY_STORAGE_USED = "hd.user.storageUsed";
    static final String PREFS_KEY_STORAGE_TOTAL = "hd.user.storageTotal";

    static void register(final String email, final String password)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_REGISTER);
        request.setBody(new Request.StringBody.Builder()
                .add(API_USER_KEY_EMAIL, email)
                .add(API_USER_KEY_PASSWORD, password)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static JsonNode getSocialToken(final String accessToken, final String provider)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_GET_SOCIAL_TOKEN);
        request.setBody(new Request.StringBody.Builder()
                .add(API_USER_KEY_SOCIAL_TOKEN, accessToken)
                .add(API_USER_KEY_SOCIAL_PROVIDER, provider)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
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
            ApiClient.checkResponseAndThrowIfNeeded(response);
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
            ApiClient.checkResponseAndThrowIfNeeded(response);
            return response.result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static long[] getStorageSize(String tokenIfNeeded)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_STORAGE_SIZE);
        if (tokenIfNeeded != null) {
            request.setHeaders(Collections.singletonMap("Auth-Token", tokenIfNeeded));
        }

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
            return new long[]{response.result.get("used_size").asLong(),
                    response.result.get("max_allowed_size").asLong()};
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static JsonNode changeAudio(File file) throws NoConnectionError, ApiResponseError, ResponseParseError {
        return uploadFile(file, API_PATH_UPDATE_AUDIO, "Audio-File-Type");
    }

    static JsonNode changeAvatar(File file) throws NoConnectionError, ApiResponseError, ResponseParseError {
        return uploadFile(file, API_PATH_UPDATE_AVATAR, "Image-Type");
    }

    static JsonNode changeBackground(File file) throws NoConnectionError, ApiResponseError, ResponseParseError {
        return uploadFile(file, API_PATH_UPDATE_BACKGROUND, "Image-Type");
    }

    private static JsonNode uploadFile(File file, String path, String headerName)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, path);
        request.setBody(new Request.FileBody("*/*", file));

        String[] comps = file.getAbsolutePath().split("\\.");
        if (comps.length > 1 && comps[comps.length -1].length() < 5) {
            request.setHeaders(Collections.singletonMap(headerName, comps[comps.length - 1]));
        }

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
            return response.result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static void saveCredentials(User user, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_USER_KEY_TOKEN, user.token);
        editor.putLong(PREFS_KEY_STORAGE_USED, user.getStorageUsed());
        editor.putLong(PREFS_KEY_STORAGE_TOTAL, user.getStorageTotal());
        for (FolderObject folder : user.privateFolders) {
            if ("фото".equalsIgnoreCase(folder.name)) {
                editor.putInt(PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID, folder.id);
            } else if ("видео".equalsIgnoreCase(folder.name)) {
                editor.putInt(PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID, folder.id);
            } else if ("музыка".equalsIgnoreCase(folder.name)) {
                editor.putInt(PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID, folder.id);
            } else if ("документы".equalsIgnoreCase(folder.name)) {
                editor.putInt(PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID, folder.id);
            }
        }
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
        raw.put(PREFS_KEY_PUBLIC_PHOTO_FOLDER_ID, prefs.getInt(PREFS_KEY_PUBLIC_PHOTO_FOLDER_ID, -1));
        raw.put(PREFS_KEY_PUBLIC_VIDEO_FOLDER_ID, prefs.getInt(PREFS_KEY_PUBLIC_VIDEO_FOLDER_ID, -1));
        raw.put(PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID, prefs.getInt(PREFS_KEY_PRIVATE_PHOTO_FOLDER_ID, -1));
        raw.put(PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID, prefs.getInt(PREFS_KEY_PRIVATE_VIDEO_FOLDER_ID, -1));
        raw.put(PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID, prefs.getInt(PREFS_KEY_PRIVATE_MUSIC_FOLDER_ID, -1));
        raw.put(PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID, prefs.getInt(PREFS_KEY_PRIVATE_DOCUMENT_FOLDER_ID, -1));
        raw.put(PREFS_KEY_STORAGE_USED, prefs.getLong(PREFS_KEY_STORAGE_USED, -1L));
        raw.put(PREFS_KEY_STORAGE_TOTAL, prefs.getLong(PREFS_KEY_STORAGE_TOTAL, -1L));
        raw.put(API_USER_KEY_CARD, Card.restoreUserCard(prefs));

        return raw;
    }
}
