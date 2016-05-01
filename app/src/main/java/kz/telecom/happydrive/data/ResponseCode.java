package kz.telecom.happydrive.data;

/**
 * Created by darkhan on 07.11.15.
 */
public interface ResponseCode {
    public static int OK = 0;
    public static int BAD_REQUEST = 1;
    public static int TOKEN_INVALID = 2;
    public static int TOKEN_EXPIRED = 3;
    public static int OAUTH_TOKEN_INVALID = 4;
    public static int UNKNOWN_OAUTH_PROVIDER = 5;
    public static int INCORRECT_EMAIL_OR_PASSWORD = 6;
    public static int NOT_FOUND = 7;
    public static int EMAIL_USED = 8;
    public static int SERVER_ERROR = 999;
}
