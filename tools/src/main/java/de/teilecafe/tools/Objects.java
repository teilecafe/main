/*
 * Date:      26.08.2012
 * Copyright: teilecafe.de
 */
package de.teilecafe.tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Funktionen die irgendwie ständig gebraucht werden, aber Java leider fehlen.
 *
 * @author Bob Tehl
 */
@SuppressWarnings("UnusedDeclaration")
public class Objects {
    /**
     * Erzeugt ein neues Array. mit all den Einträgen des alten Arrays und dem angegebenen Objekt am Ende.
     * Das neue Array hat dabei den gleichen Typ wie das Alte.
     *
     * @param array  Altes Array.
     * @param object Anzuhängendes Objekt.
     * @return Neues Array. mit all den Einträgen des alten Arrays und dem angegebenen Objekt am Ende.
     */
    public static Object[] append(final Object[] array, final Object object) {
        if (array != null) {
            final Object[] result = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
            System.arraycopy(array, 0, result, 0, array.length);
            result[array.length] = object;
            return result;
        } else if (object != null) {
            final Object[] result = (Object[]) Array.newInstance(object.getClass(), 1);
            result[0] = object;
            return result;
        } else {
            throw new IllegalArgumentException("At least one argument must be not null.");
        }
    }

    /**
     * Erzeugt ein neues Array. mit all den Einträgen des alten Arrays und dem angegebenen Wert am Ende.
     *
     * @param array Altes Array.
     * @param value Anzuhängendes Objekt.
     * @return Neues Array. mit all den Einträgen des alten Arrays und dem angegebenen Wert am Ende.
     */
    public static int[] append(final int[] array, final int value) {
        if (array != null) {
            final int[] result = new int[array.length + 1];
            System.arraycopy(array, 0, result, 0, array.length);
            result[array.length] = value;
            return result;
        } else {
            return new int[]{value};
        }
    }

    /**
     * Erzeugt aus einer Enumeration eine Liste.
     *
     * @param enumeration Enumeration die in eine Liste kopiert werden soll.
     * @return Liste mit allen Einträgen der Enumeration in der gleichen Reihenfolge.
     */
    public static <T> List<T> asList(Enumeration<T> enumeration) {
        if (enumeration == null) {
            return Collections.emptyList();
        }

        final List<T> list = new ArrayList<T>();

        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }

        return list;
    }

    /**
     * Liefert eine Liste, die alle Elemente des angegebenen Arrays enthält.
     * Im Gegensatz zu {@link java.util.Arrays#asList Arrays.asList} ist
     * diese Methode resistent gegen NullPointer.
     *
     * @param array Object-Array, welches als Liste zurückgegeben werden soll.
     * @return Eine Liste mit den Einträgen des Arrays.
     */
    public static <T> List<T> asList(T... array) {
        if (isEmpty(array)) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(array);
        }
    }

    /**
     * Liefert ein Set, welches alle Elemente des angegebenen Arrays enthält.
     * Diese Methode ist resistent gegen NullPointer.
     *
     * @param array Object-Array, welches als Set zurückgegeben werden soll.
     * @return Ein Set mit den Einträgen des Arrays.
     */
    public static <T> Set<T> asSet(T... array) {
        return new LinkedHashSet<T>(asList(array));
    }

    /**
     * Liefert ein Set, welches alle Elemente der angegebenen Collection enthält.
     * Diese Methode ist resistent gegen NullPointer.
     * Es wird ein LinkedHashSet benutzt, die Reihenfolge bleibt also erhalten.
     *
     * @param collection Collection, welche als Set zurückgegeben werden soll.
     * @return Ein Set mit den Einträgen der Collection.
     */
    public static <T> Set<T> asSet(Collection<T> collection) {
        return collection == null ? Collections.<T>emptySet() : new LinkedHashSet<T>(collection);
    }

    /**
     * Prüft ein Objekt auf <code>Null</code> und wirft bei einem
     * <code>NullPointer</code> eine IllegalArgumentException.
     *
     * @param param Zu testendes Objekt.
     * @param name  Bezeichnung für das Objekt in der Exception.
     * @throws IllegalArgumentException
     */
    public static void checkParam(final Object param, final String name) throws IllegalArgumentException {
        if (param == null) {
            throw new IllegalArgumentException("Parameter " + name + " must not be null.");
        }
    }

    /**
     * Hilfsfunktion für den Test, ob sich das angegebene Objekt im angegebenen Array befindet.
     *
     * @param array Das zu durchsuchende Array.
     * @param value Das zu suchende Objekt.
     * @return True, wenn das Objekt im Array zu finden ist.
     */
    public static boolean contains(Object[] array, Object value) {
        return asList(array).contains(value);
    }

    /**
     * Hilfsfunktion für den Test, ob ein int-Array eine bestimmten Wert enthällt.
     *
     * @param intarray Das zu durchsuchende Array.
     * @param intvalue Der zu suchende Wert.
     * @return True, wenn im Array der wert zu finden ist.
     */
    public static boolean contains(int[] intarray, int intvalue) {
        return indexOf(intarray, intvalue) > -1;
    }

    /**
     * Durchsucht die Collection, nach einem Element, welches die Filterbedingung(en) erfüllt.
     *
     * @param input  Collection mit den zu durchsuchenden Objekten
     * @param filter Filterbedingung(en)
     * @return True, die Collection enthielt ein passendes Element, sonst false.
     */
    public static <T> boolean contains(Collection<T> input, CollectionFilter<T> filter) {
        checkParam(input, "input");
        checkParam(filter, "filter");

        for (Object t : input.toArray()) {
            //noinspection unchecked
            if (filter.accept((T) t)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Kopiert den Inhalt des angegebenen Arrays in ein gleichlanges Array des angegebenen Types.
     *
     * @param array Zu kopierendes Array.
     * @param type  Typ auf dem das zurückgegebene Array basieren soll.
     * @return Ein Array vom angegebenen Array-Typ, welches die Elemente des zukopierenden Arrays enthält.
     */
    public static Object copy(final Object array, final Class type) {
        final boolean isEmpty = isEmpty(array);
        final int length = isEmpty ? 0 : Array.getLength(array);
        final Object arrayOut = Array.newInstance(type, length);

        if (!isEmpty) {
            final Class<?> srcType = array.getClass().getComponentType();

            // Wenn die Objekte des Arrays kompatibel sind, ...
            if (type.isAssignableFrom(srcType)) {
                // werden sie einfach kopiert.
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(array, 0, arrayOut, 0, length);
            }
            // Wenn der neue Typ primitiv ist und der andere sein Objekt-Pendant oder umgekehrt.
            else if (type.isPrimitive() && Classes.compatibleByClass(type, srcType) ||
                    srcType.isPrimitive() && Classes.compatibleByClass(srcType, type)) {
                for (int i = 0; i < length; i++) {
                    Array.set(arrayOut, i, Array.get(array, i));
                }
            }
        }

        return arrayOut;
    }

    /**
     * Erzeugt einen Schlüssel aus den angegebenen Objekten.
     *
     * @param objects Objekte, die Bestandteil des Schlüssels sein sollen.
     */
    public static Object createKey(final Object... objects) {
        return new CacheKey(objects);
    }

    /**
     * Prüft anhand der equals-Methode, ob die Objekte gleich sind. Dabei ist ein NullCheck integriert.
     * Wenn beide Objekte <code>null</code> sind, liefert die Methode ebenfalls <code>true</code>.<br>
     * Wenn beide Objekte Arrays sind, dann sind diese gleich, wenn sie gleich lang sind und
     * die gleichen Elemente an den gleichen Positionen enthalten.
     * <i>(Angelehnt an {@link java.util.Arrays#equals(Object[], Object[]) Arrays.equals(Object[], Object[])})</i>
     *
     * @param o1 Objekt Eins.
     * @param o2 Objekt Zwei.
     * @return true: o1=null und o2=null oder o1.equals(o2)==true; sonst false.
     * @noinspection ObjectEquality
     */
    public static boolean equal(final Object o1, final Object o2) {
        return equal(o1, o2, false);
    }

    /**
     * Prüft anhand der equals-Methode, ob die Objekte gleich sind. Dabei ist ein NullCheck integriert.
     * Wenn beide Objekte <code>null</code> sind, liefert die Methode ebenfalls <code>true</code>.<br>
     * Wenn beide Objekte Arrays sind, dann sind diese gleich, wenn sie gleich lang sind und
     * die gleichen Elemente an den gleichen Positionen enthalten.
     * <i>(Angelehnt an {@link java.util.Arrays#equals(Object[], Object[]) Arrays.equals(Object[], Object[])})</i>
     *
     * @param o1              Objekt Eins.
     * @param o2              Objekt Zwei.
     * @param nullEqualsEmpty true, wenn ein leeres Array und null gleich sein sollen
     * @return true: o1=null und o2=null oder o1.equals(o2)==true; sonst false.
     * @noinspection ObjectEquality
     */
    public static boolean equal(final Object o1, final Object o2, boolean nullEqualsEmpty) {
        // Wenn die Instanz gleich ist oder beide Objekte NULL sind, ...
        if (o1 == o2) {
            // ... können wir es kurz machen.
            return true;
        }

        // Wenn beide Objekte nicht NULL sind, ...
        if (o1 != null && o2 != null) {
            // ... und es sich um zwei Arrays handelt, ...
            if (o1.getClass().isArray() && o2.getClass().isArray()) {
                // ... dann sind diese gleich, wenn sie gleich lang sind ...
                final int size = Array.getLength(o1);
                if (size != Array.getLength(o2)) {
                    return false;
                }

                // ... und die gleichen Elemente an den gleichen Positionen enthalten.
                for (int i = 0; i < size; i++) {
                    if (!equal(Array.get(o1, i), Array.get(o2, i))) {
                        return false;
                    }
                }

                return true;
            } else {
                // Andernfalls prüfen wir die Objekte auf Gleichheit.
                return o1.equals(o2);
            }
        } else if (nullEqualsEmpty) {
            // hier ist ein Objekt null und das andere nicht und wenn das nicht-null-Objekt
            // ein leeres Array ist, dann ist das gleich null
            return o1 != null
                    ? o1.getClass().isArray() && Array.getLength(o1) == 0
                    : o2.getClass().isArray() && Array.getLength(o2) == 0;
        }

        // Es war also ein Objekt NULL und das andere nicht. Na dann sind sie wohl eher nicht gleich.
        return false;
    }

    /**
     * Liefert das erste Element einer Collection.
     */
    public static <T> T first(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }

        // Versuch mit dem potentiell schnellstmöglichen Zugriff
        Object first = Methods.executeIfPresent("getFirst", collection);

        // Bei Misserfolg ...
        if (first == null) {
            // ... Versuch mit dem Listen-Zugriff
            first = Methods.executeIfPresent("get", collection, 0);
        }

        // Wenn einer der Zugriffe fruchtete, ...
        if (first != null) {
            // ... dann haben wir unser erstes Element gefunden.

            //noinspection unchecked
            return (T) first;
        }

        // Letzter Versuch per Iterator.
        return collection.iterator().next();
    }

    /**
     * Sucht aus einer Collection alle Objekte des angegebenen Typs heraus und liefert sie in einer
     * Collection des angegebenen Typs zurück. Die Reihenfolge entspricht der, die der Iterator
     * der Quell-Collection liefert.
     *
     * @param source Collection mit den zu durchsuchenden Objekten.
     * @param clazz  Typ dem die Objekte entsprechen müssen.
     * @return Collection mit allen passenden Objekten.
     */
    public static <T> List<T> grep(final Collection source, final Class<T> clazz) {
        checkParam(source, "source");
        checkParam(clazz, "clazz");

        final List<T> result = new ArrayList<T>();
        for (Object o : source.toArray()) {
            if (clazz.isInstance(o)) {
                result.add(clazz.cast(o));
            }
        }
        return result;
    }

    /**
     * Erzeugt einen neue Collection, die nur die Elemente enthält, welche die Filterbedingung(en) erfüllt.
     * Die Reihenfolge entspricht der, die der Iterator der Quell-Collection liefert.
     *
     * @param input  Collection mit den zu durchsuchenden Objekten
     * @param filter Filterbedingung(en)
     * @return Collection mit allen passenden Objekten.
     */
    public static <T> List<T> grep(Collection<T> input, CollectionFilter<T> filter) {
        checkParam(input, "input");
        checkParam(filter, "filter");

        final List<T> output = new ArrayList<T>(input.size());

        for (final Object t : input.toArray()) {
            //noinspection unchecked
            if (filter.accept((T) t)) {
                //noinspection unchecked
                output.add((T) t);
            }
        }

        return output;
    }

    /**
     * Hilfsfunktion für den Test, an welcher Position im angegebenen int-Array sich ein bestimmter Wert befindet.
     *
     * @param intarray Das zu durchsuchende Array.
     * @param intvalue Der zu suchende Wert.
     * @return Index des Wertes im Array oder -1, wenn der Wert sich nicht im Array befindet.
     */
    public static int indexOf(int[] intarray, int intvalue) {
        if (!isEmpty(intarray)) {
            for (int i = 0; i < intarray.length; i++) {
                if (intarray[i] == intvalue) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Hilfsfunktion für den Test, an welcher Position im angegebenen Array sich das angegebene Objekt befindet.
     *
     * @param array Das zu durchsuchende Array.
     * @param value Das zu suchende Objekt.
     * @return Index des Objektes im Array oder -1, wenn das Objekt sich nicht im Array befindet.
     */
    public static int indexOf(Object[] array, Object value) {
        return asList(array).indexOf(value);
    }

    /**
     * Liefert den ersten Index des Objektes in einer Collection.
     */
    public static int indexOf(Collection collection, Object object) {
        if (collection == null || collection.isEmpty()) {
            return -1;
        }

        // Erster Zugriffsversuch mit dem potentiell schnellstmöglichen Zugriff.
        final Object index = Methods.executeIfPresent("indexOf", collection, object);

        // Wenn der Zugriff fruchtete, ...
        if (index instanceof Number) {
            // ... dann haben wir unseren Index gefunden.
            return ((Number) index).intValue();
        }

        // Notlösung per Iterator. Geht zwar immer, aber auch immer ohne Optimierung.
        int i = 0;
        for (final Iterator iter = collection.iterator(); iter.hasNext(); i++) {
            if (equal(object, iter.next())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Prüft, ob das Objekt-Array leer ist. Wenn das Objekt-Array nicht <code>null</code> ist und mindestens
     * einen Eintrag hat, dann wird <code>false</code> zurückgegeben sonst <code>true</code>.
     * <p/>
     * Diese Methode tut das Gleiche wie {@link #isEmpty(Object)}, ist aber etwas schneller, da sie ohne
     * Reflection auskommt.
     *
     * @param array Das zu prüfende Objekt-Array.
     * @return False, wenn es sich um ein Array mit mindestens einem Eintrag handelt, sonst True.
     */
    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Prüft, ob es sich bei dem Objekt um ein leeres Array handelt. Wenn das Objekt ein Array mit
     * mindestens einem Eintrag ist, dann wird <code>false</code> zurückgegeben sonst <code>true</code>.
     *
     * @param array Das zu prüfende Objekt.
     * @return False, wenn es sich um ein Array mit mindestens einem Eintrag handelt, sonst True.
     */
    public static boolean isEmpty(final Object array) {
        return array == null || !array.getClass().isArray() || Array.getLength(array) == 0;
    }

    /**
     * Liefert das letzte Element einer Collection.
     */
    public static <T> T last(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }

        // Versuch mit dem potentiell schnellstmöglichen Zugriff.
        Object last = Methods.executeIfPresent("getLast", collection);

        // Bei Misserfolg ...
        if (last == null) {
            // ... Versuch mit dem Listen-Zugriff
            last = Methods.executeIfPresent("get", collection, collection.size() - 1);
        }

        // Wenn einer der Zugriffe fruchtete, ...
        if (last != null) {
            // ... dann haben wir unser letztes Element gefunden.

            //noinspection unchecked
            return (T) last;
        }

        // WorstCase. Es ist wirklich nur eine Collection. Dann suchen wir jetzt das letzte Element per Iterator.
        T t = null;
        while (collection.iterator().hasNext()) {
            t = collection.iterator().next();
        }
        return t;
    }

    /**
     * Java-Interpretation einer Oracle-Methode zum einfachen NullValue ersetzen.
     *
     * @param value Zu prüfender Wert.
     * @param valueIfNull Rückgabe, wenn der zu prüfende Wert <code>null</code> ist.
     * @param <T> Typ der Werte.
     * @return Der Wert, wenn er nicht null war, sonst der Ersatzwert.
     */
    public static <T> T nvl(final T value, final T valueIfNull) {
        return value == null ? valueIfNull : value;
    }

    /**
     * Java-Interpretation einer Oracle-Methode zum einfachen NullValue ersetzen.
     *
     * @param value Zu prüfender Wert.
     * @param supplier Berechnung des Rückgabewertes, wenn der zu prüfende Wert <code>null</code> ist.
     * @param <T> Typ der Werte.
     * @return Der Wert, wenn er nicht null war, sonst der Ersatzwert.
     */
    public static <T> T nvl(final T value, final Supplier<T> supplier) {
        return value == null ? supplier.get() : value;
    }

    /**
     * Sortiert ein Array in die umgekehrte Reihenfolge.
     *
     * @param array Array, welches sortiert werden soll.
     */
    public static void reverse(Object array) {
        if (!isEmpty(array)) {
            final int length = Array.getLength(array);
            final Object temp = Array.newInstance(array.getClass().getComponentType(), length);
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(array, 0, temp, 0, length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, Array.get(temp, length - i - 1));
            }
        }
    }

    /**
     * Fügt zwei Arrays zusammen. Dabei wird <code>array2</code> an <code>array1</code> angehängt.
     *
     * @param array1 Erstes Array.
     * @param array2 Zweites Array.
     * @param type   Typ auf dem das zurückgegebene Array basieren soll.
     * @return Ein Array, welches die beiden angegebenen Arrays vereinigt vom angegebenen Array-Typ.
     */
    public static Object union(final Object array1, final Object array2, final Class type) {
        final boolean isEmpty1 = isEmpty(array1);
        final boolean isEmpty2 = isEmpty(array2);

        final int length1 = isEmpty1 ? 0 : Array.getLength(array1);
        final int length2 = isEmpty2 ? 0 : Array.getLength(array2);

        final Object array = Array.newInstance(type, length1 + length2);

        if (!isEmpty1) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(array1, 0, array, 0, length1);
        }

        if (!isEmpty2) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(array2, 0, array, length1, length2);
        }

        return array;
    }

    /**
     * Schneller Cache-Schlüssel.
     */
    private static class CacheKey {
        private final int hashCode;
        private final List<Object> list;

        private static List<Object> unwrap(Object object) {
            final List<Object> list = new LinkedList<Object>();

            if (object instanceof Iterable) {
                for (Object obj : (Iterable) object) {
                    list.addAll(unwrap(obj));
                }
            } else if (object != null && object.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    list.addAll(unwrap(Array.get(object, i)));
                }
            } else {
                list.add(object);
            }

            return list;
        }

        private CacheKey(Object... objects) {
            list = unwrap(objects);
            hashCode = list.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof CacheKey) || hashCode != ((CacheKey) o).hashCode
                    || list.size() != ((CacheKey) o).list.size()) {
                return false;
            }

            for (int i = 0; i < list.size(); i++) {
                final Object thisObject = list.get(i);
                final Object thatObject = ((CacheKey) o).list.get(i);

                if (thisObject == thatObject) {
                    continue;
                }

                if (thisObject == null || thatObject == null) {
                    return false;
                }

                if (thisObject.hashCode() != thatObject.hashCode()) {
                    return false;
                }

                if (!equal(thisObject, thatObject)) {
                    return false;
                }
            }
            return true;
        }

        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Übergabeinterface der Filterfunktion für <code>grep()</code>
     */
    public interface CollectionFilter<T> {
        /**
         * Filterfunktion.
         *
         * @param object zu prüfendes Objekt
         * @return <code>true</code> wenn das Objekt die Filterbedingung erfüllt
         */
        boolean accept(T object);
    }
}
