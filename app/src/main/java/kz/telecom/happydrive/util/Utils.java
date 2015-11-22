package kz.telecom.happydrive.util;

import java.util.Map;

/**
 * Created by Galymzhan Sh on 11/16/15.
 */
public class Utils {
    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() <= 0;
    }

    public static <T> T getValue(Class<?> cls, String key, T fallback, Map<String, Object> map) {
        Object obj = map.get(key);
        if (obj == null) {
            return fallback;
        }

        if (cls.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }

        return fallback;
    }

    private Utils() {
        throw new IllegalStateException(Logger.class.getSimpleName()
                + " class should never have an instance.");
    }
}
