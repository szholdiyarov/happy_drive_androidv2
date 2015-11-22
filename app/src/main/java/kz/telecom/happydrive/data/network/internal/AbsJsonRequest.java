package kz.telecom.happydrive.data.network.internal;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import kz.telecom.happydrive.data.network.ResponseParseError;

/**
 * Created by shgalym on 11/21/15.
 */
public abstract class AbsJsonRequest<T> extends AbsStringRequest<T> {
    private static ObjectMapper sObjectMapper;

    public AbsJsonRequest(Method method, String path) {
        super(method, path);
    }

    public AbsJsonRequest(Method method, String path, String host) {
        super(method, path, host);
    }

    @NonNull
    protected final JsonNode parseJsonNode(NetworkResponse networkResponse)
            throws ResponseParseError {
        String parsed = parseString(networkResponse);
        if (sObjectMapper == null) {
            sObjectMapper = new ObjectMapper();
            sObjectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            sObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        }

        try {
            // TODO server response error handling
            return sObjectMapper.readTree(parsed);
        } catch (IOException e) {
            throw new ResponseParseError("Json source parse error: " + parsed, e);
        }
    }
}
