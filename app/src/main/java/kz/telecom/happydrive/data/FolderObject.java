package kz.telecom.happydrive.data;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by shgalym on 11/22/15.
 */
public class FolderObject extends ApiObject {
    static final String API_FOLDER_ID = "folder_id";
    static final String API_FOLDER_NAME = "name";
    static final String API_IS_PUBLIC = "is_public";
    static final String API_TIMESTAMP = "timestamp";

    public final int id;
    public final String name;
    public final boolean isPublic;
    public final long timestamp;

    public FolderObject(int id, String name, boolean isPublic, long timestamp) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.timestamp = timestamp;
    }

    FolderObject(JsonNode node) {
        this.id = node.get(API_FOLDER_ID).asInt(-1);
        if (this.id < 0) {
            throw new IllegalArgumentException("node has no " + API_FOLDER_ID + " value");
        }

        this.name = node.get(API_FOLDER_NAME).asText(null);
        this.isPublic = node.get(API_IS_PUBLIC).asBoolean(false);
        this.timestamp = node.get(API_TIMESTAMP).asLong(0);
    }

    @Override
    public boolean isFolder() {
        return true;
    }
}
