package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Category {

    @JsonProperty("category_id")
    public final int id;
    public final String name;
    private static List<Category> storedCategories = new ArrayList<>();

    @JsonCreator
    public Category(@JsonProperty("category_id")int id, @JsonProperty("name")String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Category categoryById(int id) throws Exception {
        for (Category cat : storedCategories) {
            if (cat.id == id) {
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
            result = parseCategories(arrNode);
            storedCategories = result;
        } else {
            Logger.d("Couldn't get json arrayNode", "'categories' tag is empty");
        }
        return result;
    }

    private static ArrayList<Category> parseCategories(JsonNode arrNode) {
        ArrayList<Category> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (JsonNode v : arrNode) {
            try {
                Category c = mapper.treeToValue(v, Category.class);
                result.add(c);
            } catch (JsonProcessingException e) {
                Logger.d("failed to parse jsonNode to Category", e.getMessage());
            }
        }
        return result;
    }

}
