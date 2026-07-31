package tools;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Centralized in-memory cache for entity lists loaded from the database.
 * <p>
 * Each controller loads data once per session (via {@link #getOrLoad}) and
 * reuses the cached result on subsequent visits to the same view. The cache
 * is invalidated globally when a sync event arrives or when the user
 * explicitly triggers a refresh.
 * <p>
 * This replaces the old UI-page cache: views are now rebuilt fresh every
 * time (solving VirtualFlow rendering issues) while data is cached
 * separately.
 */
public final class DataCache {

    private static final ConcurrentHashMap<String, Object> cache =
        new ConcurrentHashMap<>();

    private DataCache() {}

    /**
     * Returns the cached value for {@code key}, or {@code null} if absent.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) cache.get(key);
    }

    /**
     * Returns the cached value for {@code key} if present; otherwise invokes
     * {@code loader}, stores the result, and returns it.
     * <p>
     * If the loader returns {@code null}, nothing is stored and {@code null}
     * is returned.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrLoad(String key, Supplier<T> loader) {
        Object cached = cache.get(key);
        if (cached != null) {
            return (T) cached;
        }
        T data = loader.get();
        if (data != null) {
            cache.put(key, data);
        }
        return data;
    }

    /**
     * Stores a value in the cache.  {@code null} values are ignored.
     */
    public static void put(String key, Object data) {
        if (data != null) {
            cache.put(key, data);
        }
    }

    /**
     * Invalidates a single cache entry.
     */
    public static void invalidate(String key) {
        cache.remove(key);
    }

    /**
     * Invalidates multiple cache entries at once.
     */
    public static void invalidate(String... keys) {
        for (String key : keys) {
            cache.remove(key);
        }
    }

    /**
     * Clears the entire cache.  Called on sync events so that the next
     * view visit reloads fresh data from the database.
     */
    public static void invalidateAll() {
        cache.clear();
    }
}
