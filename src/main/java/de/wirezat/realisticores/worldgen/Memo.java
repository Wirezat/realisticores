package de.wirezat.realisticores.worldgen;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Thread-safe cache of values that take long to compute. Values are computed outside any lock, and the cache
 * forgets everything once it holds its limit.
 *
 * @param <K> key type
 * @param <V> value type, never null
 */
public final class Memo<K, V> {
    private final ConcurrentMap<K, V> values = new ConcurrentHashMap<>();
    private final int limit;

    /**
     * Creates an empty cache.
     *
     * @param limit number of values after which the cache starts over
     */
    public Memo(int limit) {
        this.limit = limit;
    }

    /**
     * Gives the value of a key, computing it on first use. Two threads asking for a new key at once may both
     * compute it; both get the value stored first.
     *
     * @param key the key
     * @param compute computes the value of a missing key
     * @return the cached or new value
     */
    public V get(K key, Function<? super K, ? extends V> compute) {
        V value = values.get(key);
        if (value != null) {
            return value;
        }
        V computed = compute.apply(key);
        if (values.size() >= limit) {
            values.clear();
        }
        V stored = values.putIfAbsent(key, computed);
        return stored != null ? stored : computed;
    }
}
