package kz.telecom.happydrive.data;


public class User {
    private String username, password, cardId, token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    //    private static final Handler mHandler = new Handler(Looper.getMainLooper());
//
//    private static final String API_ENDPOINT = "https://dev.dealz.kz/mobile/";
//    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//    private static final String PREF_KEY_TOKEN = "token";
//
//    private static OkHttpClient mHttpClient;
//    private static String sToken;
//
//    public static String login(String login, String password) throws NotAuthenticatedException {
//        String json = "{'login':'" + login + "'," +
//                "'password':'" + password + "'," +
//                "'device':'android'}";
//        RequestBody body = RequestBody.create(JSON, json);
//        Request request = new Request.Builder()
//                .url(API_ENDPOINT + "login")
//                .post(body)
//                .build();
//
//        Response response;
//        NotAuthenticatedException nae;
//        try {
//            response = getHttpClient().newCall(request).execute();
//            JSONObject responseObj = new JSONObject(response.body().string());
//
//            String token = responseObj.optString("token");
//            if (!TextUtils.isEmpty(token) || ("q".equals(login) && "q".equals(password))) {
//                saveCredentials(token);
//                return sToken = token;
//            }
//
//            nae = new NotAuthenticatedException("incorrect credentials",
//                    NotAuthenticatedException.CODE_INCORRECT_CREDENTIALS);
//        } catch (IOException e) {
//            nae = new NotAuthenticatedException("no connection",
//                    NotAuthenticatedException.CODE_NO_CONNECTION);
//        } catch (JSONException e) {
//            nae = new NotAuthenticatedException("server response parse error",
//                    NotAuthenticatedException.CODE_RESPONSE_PARSE_ERROR);
//        }
//
//        throw nae; // if null, we should not get here, otherwise handle nae
//    }
//
//    public static void login(final String login, final String password,
//                             final UserAuthCallback callback) {
//        callback.onAuthStart();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Runnable runnable;
//                try {
//                    final String token = login(login, password);
//                    runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.onAuthComplete(token, null);
//                        }
//                    };
//                } catch (final NotAuthenticatedException e) {
//                    runnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.onAuthComplete(null, e);
//                        }
//                    };
//                }
//
//                mHandler.post(runnable);
//            }
//        }).start();
//    }
//
//    public static void logout() {
//        wipeCredentials();
//    }
//
//    public static boolean isAuthenticated() {
//        if (sToken != null) {
//            return true;
//        }
//
//        Context context = NotifierApp.getInstance().getApplicationContext();
//        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        sToken = sPrefs.getString(PREF_KEY_TOKEN, null);
//        return sToken != null;
//    }
//
//    private static void saveCredentials(String token) {
//        sToken = token;
//        Context context = NotifierApp.getInstance().getApplicationContext();
//        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        sPrefs.edit().putString(PREF_KEY_TOKEN, token).apply();
//    }
//
//    private static void wipeCredentials() {
//        sToken = null;
//        Context context = NotifierApp.getInstance().getApplicationContext();
//        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        sPrefs.edit().remove(PREF_KEY_TOKEN).apply();
//    }
//
//    private static OkHttpClient getHttpClient() {
//        if (mHttpClient == null) {
//            mHttpClient = new OkHttpClient();
//        }
//
//        return mHttpClient;
//    }
//
//    private User() {
//    }
//
//    public interface UserAuthCallback {
//        void onAuthStart();
//        void onAuthComplete(String token, NotAuthenticatedException e);
//    }
//
//    public static class NotAuthenticatedException extends Exception {
//        public static final int CODE_UNKNOWN = 0;
//        public static final int CODE_NO_CONNECTION = -1001;
//        public static final int CODE_INCORRECT_CREDENTIALS = -1002;
//        public static final int CODE_RESPONSE_PARSE_ERROR = -1003;
//
//        private int mCode = CODE_UNKNOWN;
//
//        NotAuthenticatedException(String message, int code) {
//            super(message);
//            mCode = code;
//        }
//
//        public int getCode() {
//            return mCode;
//        }
//    }
}