package kz.telecom.happydrive.util;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galymzhan Sh on 11/10/15.
 */
public class Logger {
    public static final String LOG_PREFIX = "hd_";

    private static final String TAG = makeLogTag("Logger", "util_");
    private static final int LOG_TAG_LENGTH_MAX = 24;

    public enum Level {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARNING(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT),
        OFF(Integer.MAX_VALUE);

        private final int origin;
        Level(int origin) {
            this.origin = origin;
        }

        public int getOrigin() {
            return origin;
        }
    }

    private static Level sLogLevel = Level.VERBOSE;

    public static String makeLogTag(@NonNull String tag, @NonNull String prefix) {
        final int prefixLength = prefix.length();
        if (tag.length() > LOG_TAG_LENGTH_MAX - prefixLength) {
            return prefix + tag.substring(0, LOG_TAG_LENGTH_MAX - prefixLength - 1);
        }

        return prefix + tag;
    }

    @NonNull
    public static String makeLogTag(@NonNull String tag) {
        return makeLogTag(tag, LOG_PREFIX);
    }

    public static synchronized void setLevel(Level level) {
        i(TAG, level != Level.OFF ? "Logger priority has changed to " + level.name()
                : "Logger is shutting down. No more log outputs will be printed.");
        sLogLevel = level;
    }

    public static void v(@NonNull String tag, @NonNull String msg) {
        log(Level.VERBOSE, tag, msg, null);
    }

    public static void v(@NonNull String tag, @NonNull String msg, @Nullable Throwable cause) {
        log(Level.VERBOSE, tag, msg, cause);
    }

    public static void d(@NonNull String tag, @NonNull String msg) {
        log(Level.DEBUG, tag, msg, null);
    }

    public static void d(@NonNull String tag, @NonNull String msg, @Nullable Throwable cause) {
        log(Level.DEBUG, tag, msg, cause);
    }

    public static void i(@NonNull String tag, @NonNull String msg) {
        log(Level.INFO, tag, msg, null);
    }

    public static void i(@NonNull String tag, @NonNull String msg, @Nullable Throwable cause) {
        log(Level.INFO, tag, msg, cause);
    }

    public static void w(@NonNull String tag, @NonNull String msg) {
        log(Level.WARNING, tag, msg, null);
    }

    public static void w(@NonNull String tag, @NonNull String msg, @Nullable Throwable cause) {
        log(Level.WARNING, tag, msg, cause);
    }

    public static void e(@NonNull String tag, @NonNull String msg) {
        log(Level.ERROR, tag, msg, null);
    }

    public static void e(@NonNull String tag, @NonNull String msg, @Nullable Throwable cause) {
        log(Level.ERROR, tag, msg, cause);
    }

    private static void log(Level level, String tag, String msg, Throwable cause) {
        if (isLoggingEnabled(level)) {
            Crashlytics.log(level.getOrigin(), tag, msg);

            if (cause != null) {
                Crashlytics.logException(cause);
            }
        }
    }

    private static boolean isLoggingEnabled(final Level level) {
        boolean isEnabled = false;
        if (level.compareTo(sLogLevel) >= 0) {
            isEnabled = true;
        }

        return isEnabled;
    }

    private Logger() {
        throw new IllegalStateException(Logger.class.getSimpleName()
                + " class should never have an instance.");
    }

    public static class Queue {
        private final List<Marker> markers = new ArrayList<>();
        private final Level level;
        private final String tag;

        private boolean mFinished = false;

        public Queue(Level level, String tag) {
            this.level = level;
            this.tag = tag;
        }

        public synchronized void add(Level level, String name, String threadName) {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished Logger.Queue");
            }

            markers.add(new Marker(level, name, threadName, SystemClock.elapsedRealtime()));
        }

        public synchronized void finish(String header) {
            if (!isLoggingEnabled(level)) {
                return;
            }

            long duration = getTotalDuration();
            log(level, tag, String.format("(%-4d ms) %s", duration, header), null);

            long prevTime = markers.get(0).time;
            for (Marker marker : markers) {
                long time = marker.time;
                log(marker.level, tag, String.format("(+%-4d) [%s] %s", time - prevTime,
                        marker.threadName, marker.event), null);
                prevTime = time;
            }

            mFinished = true;
        }

        @Override
        protected void finalize() throws Throwable {
            if (!mFinished) {
                finish("Logger.Queue on the loose");
                e(tag, "Marker log finalized without finish() - uncaught exit point");
            }

            super.finalize();
        }

        private long getTotalDuration() {
            if (markers.size() == 0) {
                return 0l;
            }

            if (markers.size() == 1) {
                return markers.get(0).time;
            }

            return markers.get(markers.size() - 1).time
                    - markers.get(0).time;
        }

        private static class Marker {
            final Level level;
            final String event;
            final String threadName;
            final long time;

            Marker(Level level, String event, String threadName, long time) {
                this.level = level;
                this.event = event;
                this.threadName = threadName;
                this.time = time;
            }
        }
    }
}
