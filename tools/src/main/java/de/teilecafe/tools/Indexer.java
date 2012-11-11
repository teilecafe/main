/*
 * Date:      26.08.2012
 * Copyright: teilecafe.de
 */
package de.teilecafe.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stellt den Index eines Objektes in einem Array bzw. einer Collection zur Verfügung.
 * Es ist sowohl der erste Index, als auch der letzte Index verfügbar.
 *
 * @author Bob Tehl
 */
@SuppressWarnings("UnusedDeclaration")
public class Indexer {
    private final Map<Object, Integer> indexMap;
    private final Map<Object, Integer> lastIndexMap;

    /**
     * Stellt den Index eines Objektes in einem Array zur Verfügung.
     * Es ist sowohl der erste Index, als auch der letzte Index verfügbar.
     */
    public Indexer(final Object[] objects) {
        this(Arrays.asList(objects));
    }

    /**
     * Stellt den Index eines Objektes in einer Collection zur Verfügung.
     * Es ist sowohl der erste Index, als auch der letzte Index verfügbar.
     */
    public Indexer(final Collection collection) {
        indexMap = new HashMap<Object, Integer>();
        lastIndexMap = new HashMap<Object, Integer>();

        int i = 0;
        for (Iterator iterator = collection.iterator(); iterator.hasNext(); i++) {
            Object o = iterator.next();
            if (!indexMap.containsKey(o)) {
                indexMap.put(o, i);
            }
            lastIndexMap.put(o, i);
        }
    }

    /**
     * Liefert den ersten Index des Objektes.
     */
    public int indexOf(final Object o) {
        final Integer index = indexMap.get(o);
        return index != null ? index : -1;
    }

    /**
     * Liefert den letzten Index des Objektes.
     */
    public int lastIndexOf(final Object o) {
        final Integer index = lastIndexMap.get(o);
        return index != null ? index : -1;
    }
}
