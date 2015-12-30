package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kz.telecom.happydrive.data.network.JsonRequest;
import kz.telecom.happydrive.data.network.NetworkManager;
import kz.telecom.happydrive.data.network.NoConnectionError;
import kz.telecom.happydrive.data.network.Request;
import kz.telecom.happydrive.data.network.Response;
import kz.telecom.happydrive.data.network.ResponseParseError;
import kz.telecom.happydrive.util.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Category {
    private static final String API_PATH_GET_CATEGORIES = "card/categories/";

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
    public static List<Category> getCategories()
            throws NoConnectionError, ApiResponseError, ResponseParseError {
        if (storedCategories.size() > 0) {
            return storedCategories;
        }

        JsonRequest request = new JsonRequest(Request.Method.GET, API_PATH_GET_CATEGORIES);

        try {
            Response<JsonNode> response = NetworkManager.execute(request);
            ApiClient.checkResponseAndThrowIfNeeded(response);
            JsonNode node = response.result.get("categories");
            return node != null ? storedCategories = parseCategories(node) : storedCategories;
        } catch (MalformedURLException e) {
            throw new ResponseParseError("malformed request sent", e);
        }
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
