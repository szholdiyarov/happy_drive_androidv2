package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.telecom.happydrive.data.network.NoConnectionError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Category {
    public final int id;
    public final String name;

    Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Category categoryById(int id) throws Exception {
        List<Category> categoryList = getCategoriesListTemp();
        for (Category cat : categoryList) {
            if (cat.id == id) {
                return cat;
            }
        }
        return null;
    }

    @NonNull
    public static List<Category> getCategoriesListTemp() throws Exception {

        JsonNode jsonNode = null;
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

        final JsonNode arrNode = jsonNode.get("categories");
        List<Category> result = new ArrayList<>();
        for (JsonNode v : arrNode) {
            result.add(parseCategory(v));
        }
        return result;
    }

    private static Category parseCategory(JsonNode jsonNode) throws ObjectParseError {
        Integer id = jsonNode.get("category_id").asInt(-1);
        String name = jsonNode.get("name").asText("");

        if (id == -1 || name == "") {
            throw new ObjectParseError("email or token is null");
        }

        return new Category(id, name);
    }
}
