package kz.telecom.happydrive.data;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class ApiResponseError extends Exception {
    public static final String API_RESPONSE_CODE_KEY = "code";

    public static final int API_RESPONSE_UNKNOWN_CLIENT_ERROR = -1;
    public static final int API_RESPONSE_CODE_OK = 0;
    public static final int API_RESPONSE_CODE_BAD_REQUEST = 1;
    public static final int API_RESPONSE_CODE_TOKEN_INVALID = 2;
    public static final int API_RESPONSE_CODE_TOKEN_EXPIRED = 3;
    public static final int API_RESPONSE_CODE_OAUTH_TOKEN_INVALID = 4;
    public static final int API_RESPONSE_CODE_OAUTH_UNKNOWN_PROVIDER = 5;
    public static final int API_RESPONSE_CODE_USER_CREDENTIALS_INVALID = 6;
    public static final int API_RESPONSE_CODE_OBJECT_NOT_FOUND = 7;
    public static final int API_RESPONSE_CODE_USER_ALREADY_EXISTS = 8;
    public static final int API_RESPONSE_CODE_CARD_INVISIBLE = 9;
    public static final int API_RESPONSE_CODE_CARD_FAVOURITE_ADD_ERROR = 10;
    public static final int API_RESPONSE_CODE_CARD_FAVOURITE_NOT_FOUND = 11;
    public static final int API_RESPONSE_CODE_CARD_FAVOURITE_ALREADY_EXIST = 12;
    public static final int API_RESPONSE_CODE_ACCESS_DENIED = 13;
    public static final int API_RESPONSE_CODE_SERVER_ERROR = 999;

    public final int apiErrorCode;

    ApiResponseError(String msg, int apiErrorCode, Throwable throwable) {
        super(msg, throwable);
        this.apiErrorCode = apiErrorCode;
    }
}
