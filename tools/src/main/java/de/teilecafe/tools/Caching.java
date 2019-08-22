package de.teilecafe.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.teilecafe.tools.Objects.equal;

/**
 * Einfache Cache-Implementierung.
 *
 * @author Bob Tehl
 */
public abstract class Caching {

    /**
     * Erzeugt einen Cache mit maximal 1000 Einträgen, die maximal
     * 500 Millisekunden zwischengespeichert werden.
     *
     * @param <K>           Typ des Schlüssels.
     * @param <V>           Typ der Daten.
     * @return Einen Cache mit den angegebenen Eigenschaften.
     */
    public static <K,V> Cache<K,V> createCache() {
        return createCache(10000, Integer.MAX_VALUE);
    }

    /**
     * Erzeugt einen einfachen Cache.
     *
     * @param size          Maximale Anzahl der Elemente im Cache.
     * @param timeToLive    Lebensdauer der Cacheeinträge in Millisekunden.
     * @param <K>           Typ des Schlüssels.
     * @param <V>           Typ der Daten.
     * @return Einen Cache mit den angegebenen Eigenschaften.
     */
    public static <K,V> Cache<K,V> createCache(final int size, final int timeToLive) {
        final SimpleCache<K, V> cache = new SimpleCache<>(size, timeToLive);
        CACHE_REPO.add(cache);
        return cache;
    }

    private static final CacheRepo CACHE_REPO = new CacheRepo();

    public static void clearAllCaches() {
        CACHE_REPO.clearAll();
    }

    /**
     * Cache mit Zugriff per Prüfsumme oder ohne.
     */
    public interface Cache<K,V> {
        /**
         * Speichert den Wert zum angegebenen Schlüssel.
         *
         * @param key      Schlüssel.
         * @param value    Wert.
         */
        void put(final K key, final V value);

        /**
         * Holt den zum Schlüssel passenden Wert.
         *
         * @param key    Schlüssel.
         * @return Wert im Cache, sofern vorhanden, sonst <code>null</code>.
         */
        V get(final K key);

        /**
         * Prüft, ob für den Schlüssel ein Wert im Cache vorliegt.
         *
         * @param key    Schlüssel.
         * @return True, wenn der Schlüssel bekannt ist, sonst false.
         */
        boolean contains(final K key);

        /**
         * Speichert den Wert zum angegebenen Schlüssel.
         * Zusätzlich wird eine Prüfsumme abgelegt, die den Wert näher beschreibt.
         *
         * @param key      Schlüssel.
         * @param value    Wert.
         * @param checksum Prüfsumme, des Wertes im Cache.
         */
        void put(final K key, final V value, final Object checksum);

        /**
         * Holt den zum Schlüssel passenden Wert, wenn der Inhalt der Prüfsumme
         * dem im Cache gespeicherten entspricht.
         *
         * @param key      Schlüssel.
         * @param checksum Prüfsumme, des Wertes im Cache.
         * @return Wert im Cache, sofern vorhanden, sonst <code>null</code>.
         */
        V get(final K key, final Object checksum);

        /**
         * Prüft, ob für den Schlüssel ein Wert im Cache vorliegt bei dem der Inhalt der
         * Prüfsumme dem im Cache gespeicherten entspricht.
         *
         * @param key      Schlüssel.
         * @param checksum Prüfsumme, des Wertes im Cache.
         * @return True, wenn der Schlüssel bekannt ist, sonst false.
         */
        boolean contains(final K key, final Object checksum);

        /**
         * Leert den Cache.
         */
        void clear();
    }

    private static class CacheRepo {
        private final List<WeakReference<Cache>> caches = new ArrayList<>();

        public final void add(final Cache cache) {
            caches.add(new WeakReference<>(cache));
        }

        public final void clearAll() {
            final Set<WeakReference> dead = new HashSet<>();
            for (final WeakReference<Cache> weakRef : caches) {
                final Cache cache = weakRef.get();
                if (cache == null) {
                    dead.add(weakRef);
                } else {
                    cache.clear();
                }
            }
            caches.removeAll(dead);
        }
    }

    private static class SimpleCache<K,V> implements Cache<K,V> {
        private static final Object NO_CHECKSUM = new Object();
        private final Map<K, CacheEntry<V>> cache;
        private final int timeToLive;

        private SimpleCache(final int count, final int timeToLive) {
            this.timeToLive = timeToLive;
            cache = new LinkedHashMap<K, CacheEntry<V>>(count, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry<K, CacheEntry<V>> eldest) {
                    return size() > count;
                }
            };
        }

        @Override
        public void put(final K key, final V value) {
            put(key, value, NO_CHECKSUM);
        }

        @Override
        public void put(final K key, final V value, final Object checksum) {
            cache.put(key, new CacheEntry<>(value, timeToLive, checksum));
        }

        @Override
        public V get(final K key) {
            return get(key, NO_CHECKSUM);
        }

        @Override
        public V get(final K key, final Object checksum) {
            final CacheEntry<V> entry = cache.get(key);
            if (entry != null && entry.isValid() && equal(entry.getChecksum(), checksum)) {
                return entry.get();
            }
            return null;
        }

        @Override
        public boolean contains(final K key) {
            return contains(key, NO_CHECKSUM);
        }

        @Override
        public boolean contains(final K key, final Object checksum) {
            final CacheEntry<V> entry = cache.get(key);
            return entry != null && entry.isValid() && equal(entry.getChecksum(), checksum);
        }

        @Override
        public void clear() {
            cache.clear();
        }

        private static class CacheEntry<V> extends FleetingValue<V> {
            private final Object checksum;

            private CacheEntry(final V value, final int timeToLive, final Object checksum) {
                super(value, timeToLive);
                this.checksum = checksum;
            }

            private Object getChecksum() {
                return checksum;
            }
        }
    }
}
