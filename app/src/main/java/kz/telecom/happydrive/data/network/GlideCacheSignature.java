package kz.telecom.happydrive.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.bumptech.glide.signature.StringSignature;

import java.util.concurrent.atomic.AtomicInteger;

import kz.telecom.happydrive.util.Logger;

/**
 * Created by shgalym on 11/27/15.
 */
public class GlideCacheSignature {
    private static final String PREFS_NAME = "cache_sig";
    private static final String PREFS_OWNER_AVA_KEY = "cache:owner:ava";
    private static final String PREFS_OWNER_BKG_KEY = "cache:owner:bkg";
    private static final String PREFS_FOREIGN_KEY = "cache:foreign";
    private static final long DEFAULT_FOREIGN_CACHE_TIME = 15 * 60 * 1000;

    private static GlideCacheSignature sCacheSignature;
    private final SharedPreferences sharedPrefs;

    private final AtomicInteger ownerAvatarIncrementer;
    private final AtomicInteger ownerBackgroundIncrementer;
    private final AtomicInteger foreginCacheIncrementer;
    private long previousCacheTime;

    public static StringSignature ownerAvatarKey(String base) {
        return new StringSignature(base + sCacheSignature.ownerAvatarIncrementer.get());
    }

    public static StringSignature ownerBackgroundKey(String base) {
        return new StringSignature(base + sCacheSignature.ownerBackgroundIncrementer.get());
    }

    public static StringSignature foreignCacheKey(String base) {
        if (System.currentTimeMillis() > sCacheSignature.previousCacheTime + DEFAULT_FOREIGN_CACHE_TIME) {
            sCacheSignature.previousCacheTime = System.currentTimeMillis();
            sCacheSignature.foreginCacheIncrementer.incrementAndGet();
            if (sCacheSignature.foreginCacheIncrementer.get() > 10000) {
                sCacheSignature.foreginCacheIncrementer.set(0);
            }
            sCacheSignature.sharedPrefs.edit().putInt(PREFS_FOREIGN_KEY,
                    sCacheSignature.foreginCacheIncrementer.get()).apply();
            Logger.i("TEST", "foreign cache incremented");
        }

        return new StringSignature(base + sCacheSignature.foreginCacheIncrementer.get());
    }

    public static void invalidateAvatarKey() {
        final int ownerValue = sCacheSignature.ownerAvatarIncrementer.incrementAndGet();
        sCacheSignature.sharedPrefs.edit().putInt(PREFS_OWNER_AVA_KEY, ownerValue).apply();
    }

    public static void invalidateBackgroundKey() {
        final int ownerValue = sCacheSignature.ownerBackgroundIncrementer.incrementAndGet();
        sCacheSignature.sharedPrefs.edit().putInt(PREFS_OWNER_BKG_KEY, ownerValue).apply();
    }

    public static void init(Context context) {
        sCacheSignature = new GlideCacheSignature(context);
    }

    private GlideCacheSignature(Context context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        final int ownerAvaInitValue = sharedPrefs.getInt(PREFS_OWNER_AVA_KEY, 0);
        final int ownerBkgInitValue = sharedPrefs.getInt(PREFS_OWNER_BKG_KEY, 0);
        final int foreignInitValue = sharedPrefs.getInt(PREFS_FOREIGN_KEY, 0);
        ownerAvatarIncrementer = new AtomicInteger(ownerAvaInitValue);
        ownerBackgroundIncrementer = new AtomicInteger(ownerBkgInitValue);
        foreginCacheIncrementer = new AtomicInteger(foreignInitValue);
    }
}
