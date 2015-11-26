package kz.telecom.happydrive.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Created by shgalym on 11/26/15.
 */
public class Comment {
    public static final String API_COMMENT_ID = "comment_id";
    public static final String API_TEXT = "text";
    public static final String API_DATE = "date";
    public static final String API_AUTHOR = "author";

    public final int id;
    public final String text;
    public final String date;
    public final Card author;

    public Comment(int id, String text, String date, Card card) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.author = card;
    }

    Comment(JsonNode node) {
        this.id = node.get(API_COMMENT_ID).asInt(-1);
        if (this.id < 0) {
            throw new IllegalArgumentException("node has no " + API_COMMENT_ID + " value");
        }

        this.text = node.get(API_TEXT).asText(null);
        this.date = node.get(API_DATE).asText(null);
        this.author = new Card(ApiClient.getObjectMapper()
                .convertValue(node.get(API_AUTHOR), Map.class));
    }
}
