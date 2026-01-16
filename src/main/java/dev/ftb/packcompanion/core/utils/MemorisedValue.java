package dev.ftb.packcompanion.core.utils;

import java.util.function.Function;

/**
 * Given a specific key, this class will memorise a value of type T. When the key is requested again, the memorised value will be returned
 * instead of recalculating it. If the key is changed or not found, the value will be recalculated using a provided function.
 * <p>
 * This class is thread-safe.
 */
public class MemorisedValue<K, T> {
    private volatile K lastKey;
    private volatile T memorisedValue;
    private final Object lock = new Object();

    /**
     * Retrieves the memorised value for the given key. If the key is different from the last requested key, the value is recalculated
     * using the provided valueProvider function.
     *
     * @param key           The key to retrieve the value for.
     * @param compute       A function that computes the value for a given key.
     *
     * @return The memorised or newly calculated value.
     */
    public T get(K key, Function<K, T> compute) {
        // Double-checked locking pattern for performance
        K currentKey = lastKey;
        if (currentKey == null || !currentKey.equals(key)) {
            synchronized (lock) {
                // Re-check condition after acquiring lock
                currentKey = lastKey;
                if (currentKey == null || !currentKey.equals(key)) {
                    lastKey = key;
                    memorisedValue = compute.apply(key);
                }
            }
        }

        return memorisedValue;
    }

    public void invalidate() {
        synchronized (lock) {
            lastKey = null;
            memorisedValue = null;
        }
    }
}
