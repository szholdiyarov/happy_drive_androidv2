package kz.telecom.happydrive.util;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class Utils {
    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() <= 0;
    }

    private Utils() {
        throw new IllegalStateException(Logger.class.getSimpleName()
                + " class should never have an instance.");
    }
}
