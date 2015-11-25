package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

import kz.telecom.happydrive.data.network.JsonRequest;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.ResponseParseError;
import kz.telecom.happydrive.util.Logger;
import kz.telecom.happydrive.util.Utils;

/**
 * Created by shgalym on 11/22/15.
 */
public class ApiClient {
    private static final String TAG = Logger.makeLogTag(ApiClient.class.getSimpleName());

    private static final String API_PATH_CARD_UPDATE = "card/update/";
    private static final String API_PATH_CARD_GET = "card/get/";
    private static final String API_PATH_CATEGORY_CARDS = "card/list/";
    private static final String API_PATH_CARD_STAR = "card/star/";
    private static final String API_PATH_CARD_UNSTAR = "card/unstar/";
    private static final String API_PATH_CARD_VISIBILITY = "card/visibility/";
    private static final String API_PATH_CARD_STARRED = "card/starred/";
    private static final String API_PATH_FILE_UPLOAD = "files/file/upload/";
    private static final String API_PATH_FILES_LIST = "files/list/";

    public static final String API_KEY_FOLDERS = "folders";
    public static final String API_KEY_FILES = "files";
    public static final String API_PATH_CHANGE_PASSWORD = "auth/change_password/";

    private static ObjectMapper sObjectMapper;

    static void updateCard(Card card) throws NoConnectionError, ApiResponseError, ResponseParseError {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put(Card.API_KEY_CATEGORY_ID, card.getCategoryId());
        cardMap.put(Card.API_KEY_FIRST_NAME, card.getFirstName());
        cardMap.put(Card.API_KEY_PHONE, card.getPhone());
        cardMap.put(Card.API_KEY_POSITION, card.getPosition());

        String lastName = card.getLastName();
        if (!Utils.isEmpty(lastName)) {
            cardMap.put(Card.API_KEY_LAST_NAME, lastName);
        }

        String email = card.getEmail();
        if (!Utils.isEmpty(email)) {
            cardMap.put(Card.API_KEY_EMAIL, email);
        }

        String address = card.getAddress();
        if (!Utils.isEmpty(address)) {
            cardMap.put(Card.API_KEY_ADDRESS, address);
        }

        String workPlace = card.getWorkPlace();
        if (!Utils.isEmpty(workPlace)) {
            cardMap.put(Card.API_KEY_WORK_PLACE, workPlace);
        }

        String shortDesc = card.getShortDesc();
        if (!Utils.isEmpty(shortDesc)) {
            cardMap.put(Card.API_KEY_SHORT_DESC, shortDesc);
        }

        String fullDesc = card.getFullDesc();
        if (!Utils.isEmpty(fullDesc)) {
            cardMap.put(Card.API_KEY_FULL_DESC, fullDesc);
        }

        String avatar = card.getAvatar();
        if (!Utils.isEmpty(avatar)) {
            cardMap.put(Card.API_KEY_AVATAR, avatar);
        }


        String cardJson = null;
        try {
            ObjectWriter writer = getObjectMapper().writer();
            cardJson = writer.writeValueAsString(cardMap);
        } catch (JsonProcessingException e) {
            Logger.e("APIClient", "json parsing error", e);
        }

        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_CARD_UPDATE);
        request.setBody(new Request.StringBody.Builder()
                .add(UserHelper.API_USER_KEY_CARD, cardJson)
                .build());

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    @NonNull
    public static Card getCard(int cardId) throws NoConnectionError, ApiResponseError, ResponseParseError {
        Map<String, String> params = new HashMap<>(1);
        params.put(Card.API_KEY_CARD_ID, cardId + "");

        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_CARD_GET);
        request.setParams(params);

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            return new Card((Map<String, Object>) getObjectMapper()
                    .convertValue(response.result, Map.class).get("card"));
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    @NonNull
    public static List<Card> getStars() throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_CARD_STARRED);

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            List<Card> result = new ArrayList<>();
            ArrayList arrayList = (ArrayList) getObjectMapper().convertValue(response.result, Map.class).get("cards");
            for (Object o: arrayList) {
                result.add(new Card((Map<String, Object>) o));
            }
            return result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    @NonNull
    public static List<Card> getCategoryCards(int categoryId) throws NoConnectionError, ApiResponseError, ResponseParseError {
        Map<String, String> params = new HashMap<>(1);
        params.put(Card.API_KEY_CATEGORY_ID, Integer.toString(categoryId));

        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_CATEGORY_CARDS);
        request.setParams(params);
        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            List<Card> result = new ArrayList<>();
            ArrayList arrayList = (ArrayList) getObjectMapper().convertValue(response.result, Map.class).get("cards");
            for (Object o: arrayList) {
                result.add(new Card((Map<String, Object>) o));
            }
            return result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    @NonNull
    public static boolean putStar(int cardId) {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_CARD_STAR);
        request.setBody(new Request.StringBody.Builder()
                .add(Card.API_KEY_CARD_ID, Integer.toString(cardId))
                .build());
        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            // Successfully stared
            return true;
        } catch (Exception ignored) {
        }
        // Failed to put star (Maybe already starred?)
        return false;
    }


    @NonNull
    public static boolean removeStar(int cardId) {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_CARD_UNSTAR);
        request.setBody(new Request.StringBody.Builder()
                .add(Card.API_KEY_CARD_ID, Integer.toString(cardId))
                .build());
        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            // Successfully unstarred
            return true;
        } catch (Exception ignored) {
        }
        // Failed to put star (Maybe already UNstarred?)
        return false;
    }

    @NonNull
    public static boolean setVisibility(boolean visible) {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_CARD_VISIBILITY);
        request.setBody(new Request.StringBody.Builder()
                .add(Card.API_KEY_VISIBILITY, Boolean.toString(visible))
                .build());
        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            // Successfully unstarred
            return true;
        } catch (Exception ignored) {
        }
        // Failed to put star (Maybe already UNstarred?)
        return false;
    }

    @NonNull
    public static boolean changePassword(String currentPassword, String newPassword) {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_CHANGE_PASSWORD);
        request.setBody(new Request.StringBody.Builder()
                .add(User.API_KEY_CURRENT_PASSWORD, currentPassword)
                .add(User.API_KEY_NEW_PASSWORD, newPassword)
                .build());
        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    @NonNull
    @WorkerThread
    public static FileObject uploadFile(int folderId, File file)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.POST, API_PATH_FILE_UPLOAD);
        request.setBody(new Request.FileBody(Request.FileBody.CONTENT_TYPE_RAW, file));

        Map<String, String> headers = new HashMap<>(2);
        headers.put("File-Name", file.getName());
        if (folderId > 0) {
            headers.put("Folder-ID", folderId + "");
        }

        request.setHeaders(headers);

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);
            return new FileObject(response.result.get("file"));
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    @WorkerThread
    public static Map<String, List<ApiObject>> getFiles(int folderId, boolean isPublic, String tokenIfNeeded)
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_FILES_LIST);
        Map<String, String> params = new HashMap<>();
        params.put(FolderObject.API_IS_PUBLIC, "false");
        if (folderId > 0) {
            params.put(FolderObject.API_FOLDER_ID, folderId + "");
        }
        if (isPublic) {
            params.put(FolderObject.API_IS_PUBLIC, "true");
        }

        if (!Utils.isEmpty(tokenIfNeeded)) {
            request.setHeaders(Collections.singletonMap("Auth-Token", tokenIfNeeded));
        }

        request.setParams(params);

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            checkResponseAndThrowIfNeeded(response);

            Map<String, List<ApiObject>> result = new HashMap<>();
            JsonNode rootNode = response.result;
            if (rootNode.hasNonNull(API_KEY_FOLDERS)) {
                JsonNode folderNode = rootNode.get(API_KEY_FOLDERS);
                if (folderNode.size() > 0) {
                    List<ApiObject> folderList = new ArrayList<>(folderNode.size());
                    for (JsonNode node : folderNode) {
                        folderList.add(new FolderObject(node));
                    }

                    result.put(API_KEY_FOLDERS, folderList);
                }
            }

            if (rootNode.hasNonNull(API_KEY_FILES)) {
                JsonNode fileNode = rootNode.get(API_KEY_FILES);
                if (fileNode.size() > 0) {
                    List<ApiObject> fileList = new ArrayList<>(fileNode.size());
                    for (JsonNode node : fileNode) {
                        fileList.add(new FileObject(node));
                    }

                    result.put(API_KEY_FILES, fileList);
                }
            }

            return result;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
    }

    static void checkResponseAndThrowIfNeeded(Response<JsonNode> response)
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

    private static ObjectMapper getObjectMapper() {
        if (sObjectMapper == null) {
            sObjectMapper = new ObjectMapper();
            sObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            sObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        }

        return sObjectMapper;
    }

    private ApiClient() {
    }
}
