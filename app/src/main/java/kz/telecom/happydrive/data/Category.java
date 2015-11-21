package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Category {
    public final int category_id;
    public final String name;

    Category(int id, String name) {
        this.category_id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Category categoryById(int id) throws Exception {
        List<Category> categoryList = getCategoriesListTemp();
        for (Category cat : categoryList) {
            if (cat.category_id == id) {
                return cat;
            }
        }
        return null;
    }

    @NonNull
    public static List<Category> getCategoriesListTemp() throws Exception {

        JsonNode jsonNode;
        try {
            jsonNode = CategoryHelper.getCategories();
        } catch (Exception ioe) {
            throw new NoConnectionError("no network error", ioe);
        }

        final int responseCode = jsonNode.hasNonNull(ApiResponseError.API_RESPONSE_CODE_KEY) ?
                jsonNode.get(ApiResponseError.API_RESPONSE_CODE_KEY)
                        .asInt(ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR) :
                ApiResponseError.API_RESPONSE_UNKNOWN_CLIENT_ERROR;

        if (responseCode != ApiResponseError.API_RESPONSE_CODE_OK) {
            throw new ApiResponseError("api response error", responseCode, null);
        }
        List<Category> result = new ArrayList<>();
        final JsonNode arrNode = jsonNode.get("categories");
        if (arrNode != null) {
            for (JsonNode v : arrNode) {
                try {
                    result.add(parseCategory(v));
                } catch (JsonProcessingException e) {
                    Logger.d("couldn't parse jsonNode to Category", e.getMessage());
                }
            }
        } else {
            Logger.d("Couldn't get json arrayNode", "'categories' tag is null");
        }
        return result;
    }

    private static Category parseCategory(JsonNode jsonNode) throws JsonProcessingException {
        return new ObjectMapper().treeToValue(jsonNode, Category.class);
    }
}
