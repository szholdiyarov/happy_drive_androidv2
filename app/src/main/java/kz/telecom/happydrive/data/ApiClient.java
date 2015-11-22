package kz.telecom.happydrive.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

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
            ObjectWriter writer = getObjectMapper().writer().withDefaultPrettyPrinter();
            cardJson = writer.writeValueAsString(cardMap);
        } catch (JsonProcessingException e) {
            Logger.e("APIClient", "json parsing error", e);
        }

        Logger.i(TAG, "cardJson: " + cardJson);
        if (cardJson == null) {
            return;
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

    public static Card getCard() {
        return null;
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

    private ApiClient() {}
}
