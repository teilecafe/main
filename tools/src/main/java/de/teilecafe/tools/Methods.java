/*
 * Date:      26.08.2012
 * Copyright: teilecafe.de
 */
package de.teilecafe.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dient dem Finden von Methoden in Objekten oder Klassen per Reflection.
 * <p/>
 * Zusätzlich stehen auch statische Methoden für einfache Suchen und
 * direktes Ausführen gefundener Methoden zur Verfügung.
 *
 * @author Bob Tehl
 */
@SuppressWarnings({"UnusedDeclaration", "WeakerAccess", "TryWithIdenticalCatches"})
public final class Methods {
    // Methoden-Cache, der das Suchen von Methoden beschleunigt.
    private static final ThreadLocal<Map<Object, Method>> METHOD_CACHE =
            new ThreadLocal<Map<Object, Method>>() {
                @Override
                protected Map<Object, Method> initialValue() {
                    return new LinkedHashMap<Object, Method>(1000, 0.75f, true) {
                        /**
                         * Der älteste Eintrag soll entfernt werden, wenn die Map ihre maximale Größe erreicht hat.
                         */
                        protected boolean removeEldestEntry(Map.Entry<Object, Method> eldest) {
                            return size() > 999;
                        }
                    };
                }
            };
    private static final Object[] NO_ARGS = new Object[0];
    private final Class clazz;

    private final Method[] methods;
    private final Predicate predicate;

    /**
     * Erzeugt ein Predicate, welches der Prüfung des Methodennamens nach seinem Ende dient.
     * Dieses Predikat arbeitet casesensitiv.
     *
     * @param methodNameEndsWith String mit dem der Methodenname enden soll.
     * @return Predicate zur Prüfung des Endes des Methodennamens.
     */
    public static Predicate endsWith(String methodNameEndsWith) {
        return new AbstractStringPredicate(methodNameEndsWith) {
            public boolean matches(final Method method) {
                return Classes.endsWith(method, getPrediacteString());
            }
        };
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final String methodName, final Object object) {
        Objects.checkParam(methodName, "methodName");
        Objects.checkParam(object, "object");

        return execute(isEqual(methodName), object, NO_ARGS);
    }

    /**
     * Sucht die Methode anhand eines Prädikates und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Predicate predicate, final Object object) {
        final Method method = find(predicate, object);
        if (method == null) {
            throw new NoSuchMethodFoundException(predicate.toString(), object);
        }
        return execute(method, object, NO_ARGS);
    }

    /**
     * Führt die angegebene Methode aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     *
     * @param method Auszuführende Methode.
     * @param object Objekt, auf dem die Methode ausgeführt werden soll.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Method method, final Object object) {
        return execute(method, object, NO_ARGS);
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und Parameter und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @param arg        Argument für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final String methodName, final Object object, final Object arg) {
        return execute(isEqual(methodName), object, arg);
    }

    /**
     * Sucht die Methode anhand eines Prädikates und eines Parameter und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @param arg       Argument für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Predicate predicate, final Object object, final Object arg) {
        final Method method = find(predicate, object, arg);
        if (method == null) {
            throw new NoSuchMethodFoundException(predicate.toString(), object, arg);
        }
        return execute(method, object, arg);
    }

    /**
     * Sucht die Methode anhand eines Prädikates und mit den angegebenen Parametern und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args       Argumente für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final String methodName, final Object object, final Object[] args) {
        return execute(isEqual(methodName), object, args);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und Parametern und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird eine
     * {@link NoSuchMethodFoundException NoSuchMethodFoundException}
     * geworfen.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args      Argumente für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Predicate predicate, final Object object, final Object[] args) {
        final Method method = find(predicate, object, args);
        if (method == null) {
            throw new NoSuchMethodFoundException(predicate.toString(), object, args);
        }
        return execute(method, object, args);
    }

    /**
     * Führt die angegebene Methode aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     *
     * @param method Auszuführende Methode.
     * @param object Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args   Argumente für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Method method, final Object object, final Object[] args) {
        Objects.checkParam(method, "method");
        Objects.checkParam(object, "object");

        try {
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new ExecutionException(method, object, args, e);
        } catch (InvocationTargetException e) {
            throw new ExecutionException(method, object, args, e.getCause());
        } catch (RuntimeException e) {
            throw new ExecutionException(method, object, args, e);
        }
    }

    /**
     * Führt die angegebene Methode aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird
     * die Exception als Laufzeitfehler geworfen.
     *
     * @param method Auszuführende Methode.
     * @param object Objekt, auf dem die Methode ausgeführt werden soll.
     * @param arg    Argument für den Methodenaufruf.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object execute(final Method method, final Object object, final Object arg) {
        return execute(method, object, new Object[]{arg});
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final Predicate predicate, final Object object) {
        final Method method = find(predicate, object);
        return method == null ? null : execute(method, object);
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param methodName Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final String methodName, final Object object) {
        Objects.checkParam(methodName, "methodName");
        Objects.checkParam(object, "object");

        return executeIfPresent(isEqual(methodName), object, NO_ARGS);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @param arg       Argument der gesuchten Methode.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final Predicate predicate, final Object object, final Object arg) {
        final Method method = find(predicate, object, arg);
        return method == null ? null : execute(method, object, arg);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args      Argumente der gesuchten Methode.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final Predicate predicate, final Object object, final Object[] args) {
        final Method method = find(predicate, object, args);
        return method == null ? null : execute(method, object, args);
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param methodName Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @param arg        Argument der gesuchten Methode.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final String methodName, final Object object, final Object arg) {
        final Method method = find(isEqual(methodName), object, arg);
        return method == null ? null : execute(method, object, arg);
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und führt sie aus.
     * Im Falle einer Ausnahme bei der Ausführung der Methode, wird die Exception als Laufzeitfehler geworfen.
     * Sollte die Methode nicht gefunden werden, wird lediglich <code>null</code> zurückgegeben.
     *
     * @param methodName Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args       Argumente der gesuchten Methode.
     * @return Rückgabewert des Methodenaufrufs.
     */
    public static Object executeIfPresent(final String methodName, final Object object, final Object[] args) {
        final Method method = find(isEqual(methodName), object, args);
        return method == null ? null : execute(method, object, args);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung. Die Methode darf keine Parameter haben.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, in dem die Methode gesucht werden soll.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Object object) {
        Objects.checkParam(object, "object");
        return find(predicate, object.getClass());
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen. Die Methode darf keine Parameter haben.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, in dem die Methode gesucht werden soll.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final String methodName, final Object object) {
        Objects.checkParam(methodName, "methodName");
        Objects.checkParam(object, "object");

        return find(isEqual(methodName), object);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung. Die Methode darf keine Parameter haben.
     *
     * @param predicate Beschreibung der Methode.
     * @param clazz     Klasse, in der die Methode gesucht werden soll.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Class clazz) {
        Objects.checkParam(predicate, "predicate");
        Objects.checkParam(clazz, "clazz");

        final Object key = Objects.createKey(predicate, clazz);
        Method method = METHOD_CACHE.get().get(key);

        if (method == null && !METHOD_CACHE.get().containsKey(key)) {
            final Methods finder = new Methods(clazz, new BestMatchPredicate() {
                public boolean matches(final Method method) {
                    return predicate.matches(method) && Classes.paramCount(method) == 0;
                }

                public Method findBestFittingMethod(final Method[] methods) {
                    // Sollte nicht vorkommen, aber ...
                    throw new IllegalStateException("Multiple methods found with equal names and all without params."
                            + " This is not good and should not happen.");
                }
            });

            method = finder.getBestMethod();

            METHOD_CACHE.get().put(key, method);
        }

        return method;
    }

    /**
     * Sucht die Methode mit dem angegebenen Prädikat und dem angegebenen Parameter.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, in dem die Methode gesucht werden soll.
     * @param arg       Argument für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Object object, final Object arg) {
        Objects.checkParam(object, "object");
        return find(predicate, object.getClass(), arg != null ? arg.getClass() : null);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und den angegebenen Parametern.
     *
     * @param predicate Beschreibung der Methode.
     * @param object    Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args      Argumente für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Object object, final Object[] args) {
        Objects.checkParam(object, "object");
        return find(predicate, object.getClass(), Classes.getClasses(args));
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und dem angegebenen Parameter.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, in dem die Methode gesucht werden soll.
     * @param arg        Argument für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final String methodName, final Object object, final Object arg) {
        return find(isEqual(methodName), object, arg);
    }

    /**
     * Sucht die Methode mit dem angegebenen Prädikat und dem angegebenen Parameter.
     *
     * @param predicate     Beschreibung der Methode.
     * @param clazz         Klasse, in der die Methode gesucht werden soll.
     * @param argumentClass Argumentklasse für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Class clazz, final Class argumentClass) {
        Objects.checkParam(predicate, "predicate");
        Objects.checkParam(clazz, "clazz");

        final Object key = Objects.createKey(predicate, clazz, argumentClass);
        Method method = METHOD_CACHE.get().get(key);

        if (method == null && !METHOD_CACHE.get().containsKey(key)) {
            final Methods finder = new Methods(clazz, new SimpleBestMatchPredicate(predicate,
                    argumentClass == null ? null : new Class[]{argumentClass}));
            method = finder.getBestMethod();

            METHOD_CACHE.get().put(key, method);
        }

        return method;
    }

    /**
     * Sucht die Methode mit dem angegebenen Namen und den angegebenen Parametern.
     *
     * @param methodName Der Name der Methode.
     * @param object     Objekt, auf dem die Methode ausgeführt werden soll.
     * @param args       Argumente für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final String methodName, final Object object, final Object[] args) {
        return find(isEqual(methodName), object, args);
    }

    /**
     * Sucht die Methode mit der angegebenen Beschreibung und den angegebenen Parametern.
     *
     * @param predicate Beschreibung der Methode.
     * @param clazz     Klasse, in der die Methode gesucht werden soll.
     * @param args      Argumente für den Methodenaufruf.
     * @return Gefundene Methode oder null, wenn keine gefunden werden konnte.
     */
    public static Method find(final Predicate predicate, final Class clazz, final Class[] args) {
        Objects.checkParam(predicate, "predicate");
        Objects.checkParam(clazz, "clazz");

        final Object key = Objects.createKey(predicate, clazz, args);
        Method method = METHOD_CACHE.get().get(key);

        if (method == null && !METHOD_CACHE.get().containsKey(key)) {
            final Methods finder = new Methods(clazz, new SimpleBestMatchPredicate(predicate, args));
            method = finder.getBestMethod();

            METHOD_CACHE.get().put(key, method);
        }

        return method;
    }

    /**
     * Sucht alle Methoden in der angegebenen Instanz, die dem Prädikat entsprechen.
     *
     * @param predicate Prädikat, welches die akzeptierten Methoden definiert.
     * @param instance  Instanz, die zu durchsuchen ist.
     * @return Alle Methoden, die durch das angegebene Prädikat akzeptiert wurden.
     */
    public static Method[] findAll(final Predicate predicate, final Object instance) {
        Objects.checkParam(predicate, "predicate");
        Objects.checkParam(instance, "instance");

        return findAll(predicate, instance.getClass());
    }

    /**
     * Sucht alle Methoden in der angegebenen Klasse, die dem Prädikat entsprechen.
     *
     * @param predicate Prädikat, welches die akzeptierten Methoden definiert.
     * @param clazz     Klasse, die zu durchsuchen ist.
     * @return Alle Methoden, die durch das angegebene Prädikat akzeptiert wurden.
     */
    public static Method[] findAll(final Predicate predicate, final Class clazz) {
        Objects.checkParam(predicate, "predicate");
        Objects.checkParam(clazz, "clazz");

        final Methods finder = new Methods(clazz, predicate);
        return finder.getMethods();
    }

    /**
     * Erzeugt ein Predicate, welches der Prüfung des Methodennamens dient.
     * Dieses Predikat arbeitet casesensitiv.
     *
     * @param methodNamesEquals String mit dem der Methodenname gleich sein soll.
     * @return Predicate zur vollständigen Prüfung des Methodennamens.
     */
    public static Predicate isEqual(String methodNamesEquals) {
        return new AbstractStringPredicate(methodNamesEquals) {
            public boolean matches(final Method method) {
                return Classes.equals(method, getPrediacteString());
            }
        };
    }

    /**
     * Erzeugt ein Predicate, welches der Prüfung des Methodennamens nach seinem Anfang dient.
     * Dieses Predikat arbeitet casesensitiv.
     *
     * @param methodNameStartsWith String mit dem der Methodenname beginnen soll.
     * @return Predicate zur Prüfung des Anfangs des Methodennamens.
     */
    public static Predicate startsWith(String methodNameStartsWith) {
        return new AbstractStringPredicate(methodNameStartsWith) {
            public boolean matches(final Method method) {
                return Classes.startsWith(method, getPrediacteString());
            }
        };
    }

    /**
     * Erzeugt eine neue Methods-Instanz.
     *
     * @param clazz Klasse, in welcher die Methoden gesucht werden sollen.
     */
    protected Methods(final Class clazz, final Predicate predicate) {
        this.clazz = clazz;
        this.predicate = predicate;
        Objects.checkParam(clazz, "clazz");
        Objects.checkParam(predicate, "predicate");

        // Methoden suchen
        final Collection<Method> result = new ArrayList<Method>();
        Method[] methods = this.clazz.getMethods();
        for (Method method : methods) {
            if (this.predicate.matches(method)) {
                result.add(method);
            }
        }
        methods = result.toArray(new Method[result.size()]);

        // Wenn wir ein FilterPredicate haben, dann wird hier gefiltert.
        if (this.predicate instanceof FilterPredicate && !Objects.isEmpty(methods)) {
            methods = ((FilterPredicate) this.predicate).filter(methods);
        }

        this.methods = methods;
    }

    /**
     * Liefert alle Methoden, die gefunden, durch das Predicate akzeptiert und vom evtl. vorhandenen
     * FilterPredicate zusammengefasst wurden.
     *
     * @return Alle gefundenen Methoden oder ein leeres Array.
     */
    public final Method[] getMethods() {
        return methods;
    }

    /**
     * Gibt die die passendste gefundene Methode zurück. Wenn gar keine Methode gefunden wurde, liefert
     * der Aufruf <code>null</code>.
     * Sollten mehrere Methoden verfügbar sein, wird ein BestMatchPredicate benötigt.
     * Wenn dieses verfügbar ist, dann wird dort {@link BestMatchPredicate#findBestFittingMethod findBestFittingMethod}
     * aufgerufen und das Resultat zurückgegeben. Andernfalls wird eine DuplicateMethodException geworfen.
     */
    public final Method getBestMethod() {
        final int methodCount = methodCount();

        // Keine Methode gefunden?
        if (methodCount == 0) {
            // Dann wars halt nix mit der Suche.
            return null;
        }

        // Genau eine Methode gefunden?
        if (methodCount == 1) {
            // Dann ist dies auch die Beste.
            return methods[0];
        }

        // Hier sind also mehrere Methoden und wir wollen aber nur die Beste haben.

        // Wenn wir ein BestMatchPredicate haben, dann ist dies noch möglich.
        if (predicate instanceof BestMatchPredicate) {
            return ((BestMatchPredicate) predicate).findBestFittingMethod(getMethods());
        }

        throw new DuplicateMethodException(clazz);
    }

    /**
     * Liefert die Anzahl der gefundenen Methoden.
     */
    public int methodCount() {
        return methods.length;
    }

    /**
     * Einfache Basisimplementierung für ein String-Prädikat.
     */
    public abstract static class AbstractStringPredicate implements Predicate {
        private final String oFullQualifiedIdentifier;

        // Identifikatoren der Instanz
        private final int oHashCode;
        // Instanzkennung.
        private final String oIdentifier;

        /**
         * Konstruktor.
         *
         * @param identifier Instanzkennung.
         */
        protected AbstractStringPredicate(String identifier) {
            oIdentifier = identifier;

            // Identifikatoren der Instanz erzeugen
            oHashCode = oIdentifier.hashCode();
            oFullQualifiedIdentifier = getClass().getName() + ": " + oIdentifier;
        }

        /**
         * Gibt an, ob das angegebene Object gleich diesem ist.
         * Um Probleme mit serialsierten Objekten zu vermeiden, wird hier sowohl die Instanzkennung als auch der
         * Klassenname (anstatt der Klasse selbst) verglichen.
         */
        @Override
        public boolean equals(Object object) {
            // Gleiche Objektinstanz => schneller positiver Abbruch
            if (object == this) {
                return true;
            }

            // Zu vergleichendes Objekt ist nicht vom selben Typ oder
            // der HashCode ist anders => schneller negativer Abbruch
            if (!(object instanceof AbstractStringPredicate) || oHashCode != object.hashCode()) {
                return false;
            }

            // Vergleich des Klassennamens und des Identifiers um bei serialisierten Klassen nicht Probleme durch
            // unterschiedliche Klassenobjekte trotz gleicher Klasse aber verschiedener Classloader zu bekommen.
            return oFullQualifiedIdentifier.equals(object.toString());
        }

        /**
         * Liefert den HashCode des Identifiers.
         */
        public final int hashCode() {
            return oHashCode;
        }

        /**
         * Gibt die Klasse und die Instanzkennung des Objektes zurück.
         *
         * @return Voll qualifizierte Instanzkennung.
         */
        public final String toString() {
            return oFullQualifiedIdentifier;
        }

        /**
         * Liefert den Identifier für abgeleitete Typen.
         */
        public final String getIdentifier() {
            return oIdentifier;
        }

        /**
         * Liefert den enthaltenen String.
         */
        protected String getPrediacteString() {
            return getIdentifier();
        }
    }

    /**
     * Erweitertes Predicate, welches nach dem grundsätzlichen Akzeptieren von Methoden,
     * in der Lage ist die einzig wahre Methode zu erkennen und zurückzugeben.
     */
    public interface BestMatchPredicate extends Predicate {
        /**
         * Ermittelt aus den Methoden die am Besten passende.
         * Wird aufgerufen, wenn mehrere Methoden gefunden wurden und {@link Methods#getBestMethod()}
         * aufgerufen, also nur eine einzige Methode gesucht wird.
         *
         * @param methods Alle gefundenen Methoden.
         * @return Die am Besten geeignete Methode.
         */
        Method findBestFittingMethod(final Method[] methods);
    }

    /**
     * RuntimeException, die anzeigt, dass eine bestimmte Methode nicht eindeutig identifiziert werden konnte.
     */
    public static class DuplicateMethodException extends MethodRuntimeException {
        /**
         * Erzeugt eine neue DuplicateMethodException.
         *
         * @param clazz Klasse in der die Methode gesucht wurde.
         */
        public DuplicateMethodException(Class clazz) {
            super("Duplicate method in " + clazz + ". Predicate must be an instance of BestMatchPredicate.");
        }
    }

    /**
     * Ausführlicher Laufzeitfehler, der das Problem bei der Ausführung beschreibt.
     */
    public static class ExecutionException extends MethodRuntimeException {
        /**
         * Erzeugt eine neue ExecutionException.
         *
         * @param method Methode, in der der Fehler auftrat.
         * @param cause  Aufgetretene Exception.
         */
        private ExecutionException(Method method, Object object, Object[] args, Throwable cause) {
            super("Error while executing method " + method.getName()
                    + "(" + toCommaString(args) + ")" + " in " + object + ".", cause);
        }
    }

    /**
     * Erweitertes Predicate, welches nach dem grundsätzlichen Akzeptieren von Methoden,
     * die Möglichkeit bietet die Auswahl zu verfeinern.
     */
    public interface FilterPredicate extends Predicate {
        /**
         * Filtert die Methoden, es könnten also z.B. mehrdeutige Methoden zusammengefasst und
         * unerwünschte entfernt werden.
         *
         * @param methods zu filternde Methoden.
         * @return Gefilterte Methoden.
         */
        Method[] filter(final Method[] methods);
    }

    protected static abstract class MethodRuntimeException extends RuntimeException {
        /**
         * Gibt einen kommaseparierten String mit den Elementen eines Arrays zurück.
         *
         * @param array - Das Array mit den zu verkettenden Objekten
         */
        protected static String toCommaString(Object[] array) {
            if (array == null || array.length == 0) {
                return "";
            }

            StringBuilder val = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    val.append(", ");
                }
                val.append(array[i]);
            }

            return val.toString();
        }

        protected MethodRuntimeException(String message) {
            super(message);
        }

        protected MethodRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * RuntimeException, die anzeigt, dass eine bestimmte Methode nicht gefunden werden konnte
     * und damit die Ausführung nicht möglich ist.
     */
    public static class NoSuchMethodFoundException extends MethodRuntimeException {
        /**
         * Erzeugt eine neue NoSuchMethodFoundException.
         *
         * @param methodName Name der Methode.
         * @param object     Objekt in dem die Methode gesucht wurde.
         */
        private NoSuchMethodFoundException(String methodName, Object object) {
            super("Unknown method " + methodName + " in " + object);
        }

        /**
         * Erzeugt eine neue NoSuchMethodFoundException.
         *
         * @param methodName Name der Methode.
         * @param object     Objekt in dem die Methode gesucht wurde.
         * @param arg        Argument für den Methodenaufruf.
         */
        private NoSuchMethodFoundException(String methodName, Object object, Object arg) {
            super("Unknown method " + methodName + "(" + arg + ")" + " in " + object);
        }

        /**
         * Erzeugt eine neue NoSuchMethodFoundException.
         *
         * @param methodName Name der Methode.
         * @param object     Objekt in dem die Methode gesucht wurde.
         * @param args       Argumente für den Methodenaufruf.
         */
        private NoSuchMethodFoundException(String methodName, Object object, Object[] args) {
            super("Unknown method " + methodName + "(" + toCommaString(args) + ")" + " in " + object);
        }
    }

    /**
     * Stellt innerhalb eines MethodFinders fest, welche Methoden der im Methods
     * angegebenen Klasse akzeptiert werden.
     */
    public interface Predicate {
        /**
         * Stellt fest, ob die angegebene Methode akzeptiert werden soll oder nicht.
         */
        boolean matches(final Method method);
    }

    /**
     * Einfaches BestMatchPredicate, welches ein Predicate dekoriert und ein Argument oder eine Menge von
     * Argumenten zur Bestimmung der besten Methode benutzt.
     */
    public static class SimpleBestMatchPredicate implements BestMatchPredicate {
        private final Class[] oArguments;
        private final Predicate oPredicate;

        public SimpleBestMatchPredicate(final Predicate predicate, final Class[] arguments) {
            oPredicate = predicate;
            oArguments = arguments;
        }

// --------------------- Interface BestMatchPredicate ---------------------

        /**
         * Sucht die am Besten zu den Argumenten passende Methode aus der Menge.
         */
        public Method findBestFittingMethod(final Method[] methods) {
            return Classes.findBestFittingMethod(methods, oArguments);
        }

// --------------------- Interface Predicate ---------------------

        /**
         * Stellt fest, ob die angegebene Methode akzeptiert werden soll oder nicht.
         */
        public boolean matches(final Method method) {
            if (oPredicate.matches(method) && Classes.paramCount(method) == getArgumentsLength()) {
                for (int i = 0; i < getArgumentsLength(); i++) {
                    if (!Classes.compatibleByClass(Classes.getParamType(method, i), oArguments[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private int getArgumentsLength() {
            return oArguments == null ? 0 : oArguments.length;
        }
    }
}
