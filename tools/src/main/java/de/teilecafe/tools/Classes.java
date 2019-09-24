/*
 * Date:      26.08.2012
 * Copyright: teilecafe.de
 */
package de.teilecafe.tools;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static de.teilecafe.tools.Objects.asSet;

/**
 * Funktionen, deren Benutzung die Arbeit mit Reflection erleichtern
 * oder einfach nur nützlich sind im Umgang mit Klassen und Methoden.
 *
 * @author Bob Tehl
 */
@SuppressWarnings("UnusedDeclaration")
public class Classes
{
    public static final Class[] EMPTY_CLASS_ARRAY = {};
    public static final String TYPE_FIELD_NAME = "TYPE";

    /**
     * Prüft, ob die Instanz zu der angegebenen Klasse prinzipiell kompatibel ist.
     *
     * @param clazz     Klasse.
     * @param instance  Instanz.
     * @return True, wenn die Instanz <code>null</code> ist bzw. zur Klasse "gecastet" werden könnte.
     */
    public static boolean compatible(Class clazz, Object instance)
    {
        return instance == null || compatibleByClass(clazz, instance.getClass());
    }

    /**
     * Prüft, ob die Klasse <code>otherClass</code> zu der Klasse <code>clazz</code> prinzipiell kompatibel ist.
     *
     * @param clazz      Klasse.
     * @param otherClass Zu testende Klasse.
     * @return True, wenn die Klasse <code>null</code> ist bzw. zur Klasse "gecastet" werden könnte.
     */
    public static boolean compatibleByClass(Class clazz, Class otherClass)
    {
        Objects.checkParam(clazz, "clazz");

        // Wenn es sich um einen NullPointer handelt, ...
        if (otherClass == null)
        {
            // ... dann ist alles ok, da ein NullPointer jedes beliebige Objekt repräsentieren darf.
            return true;
        }

        // Wenn der instanceOf-Vergleich klappt, ...
        if (clazz.isAssignableFrom(otherClass))
        {
            // ... dann ist auch alles ok.
            return true;
        }

        // Wenn es sich um einen primitiven DatenTyp handelt, ...
        if (clazz.isPrimitive())
        {
            try
            {
                // ... dann versuchen wir rauszukriegen, ob die Instanz zu diesem kompatibel wäre.
                final Field field = otherClass.getField(TYPE_FIELD_NAME);
                final Class type = field == null ? null : (Class)field.get(otherClass);
                return type != null && clazz.isAssignableFrom(type);
            }
            catch (NoSuchFieldException e)
            {
                // Wenn's kracht, isses nich kompatibel
            }
            catch (IllegalAccessException e)
            {
                // Wenn's kracht, isses nich kompatibel
            }
        }

        // Sonst sind die Teile halt nicht kompatibel.
        return false;
    }

    /**
     * Erzeugt einen einfachen Proxy für die angegebenen Interfaces.
     * Alle in den Interfaces deklarierten Methoden werden an den angegebenen Handler übergeben.
     * Die Objekt-Methoden werden intern behandelt und nicht weitergereicht.
     *
     * @param handler    Der Handler.
     * @param interfaces Die zu "handlenden" Interfaces.
     * @return Die Proxy-Instanz.
     */
    public static Object createProxy(final InvocationHandler handler, final Class... interfaces)
    {
        return Proxy.newProxyInstance(Classes.class.getClassLoader(), interfaces, new SerializableInvocationHandler() {
            private final Collection oInterfaces = Objects.asSet(interfaces);
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if (oInterfaces.contains(method.getDeclaringClass()))
                {
                    return handler.invoke(proxy, method, args);
                }
                else if (Object.class.equals(method.getDeclaringClass()))
                {
                    if ("equals".equals(method.getName()))
                    {
                        return proxy == args[0];
                    }
                    // Objekt-Methoden
                    return method.invoke(this, args);
                }

                // wenn kein passende methode gefunden wurde, ist das ein Problem
                throw new NoSuchMethodException("Unknown method " + method.getName());
            }
        });
    }

    /**
     * Liefert den Abstand zwischen den beiden Klassen, wenn sie miteinander verwandt sind.
     *
     * <pre>
     * Beispiel:
     * ... class "SuperKlasse" extends "SuperSuperKlasse" implements "Schnittstelle-C" ...
     * ... interface "Schnittstelle-A" extends "SuperSchnittstelle"
     * ... class "Klasse" extends "SuperKlasse" implements "Schnittstelle-A", "Schnittstelle-B" ...
     *
     * Der jeweilige Abstand von "Klasse":
     *      - "Klasse"              = 0
     *      - "Schnittstelle-A"     = 1
     *      - "Schnittstelle-B"     = 2
     *      - "SuperSchnittstelle"  = 3
     *      - "SuperKlasse"         = 4
     *      - "Schnittstelle-C"     = 5
     *      - "SuperSuperKlasse"    = 6
     * </pre>
     *
     * @param classOne Klasse Eins.
     * @param classTwo Klasse Zwei.
     * @return Abstand der beiden Klassen. Wenn sie nicht verwandt sind, dann wird
     *         {@link Integer#MAX_VALUE} zurückgegeben, was einen sehr hohen Abstand ausdrückt.
     */
    public static int difference(final Class classOne, final Class classTwo)
    {
        final Class parent;
        final Class child;

        if (classOne == null || classTwo == null)
        {
            return Integer.MAX_VALUE;
        }
        else if (classOne.equals(classTwo))
        {
            return 0;
        }
        else if (classOne.isAssignableFrom(classTwo))
        {
            parent = classOne;
            child = classTwo;
        }
        else if (classTwo.isAssignableFrom(classOne))
        {
            parent = classTwo;
            child = classOne;
        }
        else
        {
            return Integer.MAX_VALUE;
        }

        final Indexer indexer = new Indexer(getSuperClasses(child));
        return indexer.indexOf(parent) - indexer.indexOf(child);
    }

    /**
     * Stellt fest, ob der Methodenname mit einem der angegebenen Texte endet.
     * NullPointer führen zu <code>false</code>.
     */
    public static boolean endsWith(final Method method, final String... strings)
    {
        if (method == null || Objects.isEmpty(strings))
        {
            return false;
        }

        for (final String string : strings)
        {
            if (method.getName().endsWith(string))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Stellt fest, ob der Methodenname mit einem der angegebenen Texte übereinstimmt.
     * NullPointer führen zu <code>false</code>.
     */
    public static boolean equals(final Method method, final String... strings)
    {
        if (method == null || Objects.isEmpty(strings))
        {
            return false;
        }

        for (final String string : strings)
        {
            if (method.getName().equals(string))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Sucht die passendste Methode für den angegebenen Parameter.
     * Dabei wird nur der erste Parameter benutzt. D.h., die Methode darf mehr als einen Parameter haben,
     * sollte aber mindestens einen Parameter haben.
     *
     * @param methods           Methoden, die durchsucht werden sollen.
     * @param desiredParamClass Gewünschte Klasse des Parameters.
     * @return Die Methode deren Parameter am dichtesten an der gewünschten Klasse liegt.
     */
    public static Method findBestFittingMethod(final Method[] methods, final Class desiredParamClass)
    {
        int lastDiff = Integer.MAX_VALUE;
        Method chosen = null;

        for (final Method method : methods) {
            final int diff = paramCount(method) == 0 ? Integer.MAX_VALUE :
                    difference(desiredParamClass, getParamType(method, 0));

            if (chosen == null || diff < lastDiff) {
                chosen = method;
                lastDiff = diff;
            }
        }

        return chosen;
    }

    /**
     * Sucht die passendste Methode für die angegebenen Parameter.
     *
     * @param methods             Methoden, die durchsucht werden sollen.
     * @param desiredParamClasses Gewünschte Klassen der Parameter.
     * @return Die Methode deren Parameter am dichtesten an der gewünschten Klasse liegen.
     */
    public static Method findBestFittingMethod(final Method[] methods, final Class[] desiredParamClasses)
    {
        long lastDiff = Long.MAX_VALUE;
        Method chosen = null;

        for (final Method method : methods) {
            final Class[] methodClasses = method.getParameterTypes();

            long diff = Long.MAX_VALUE;
            if (methodClasses != null && desiredParamClasses != null
                    && methodClasses.length == desiredParamClasses.length) {
                diff = 0;
                for (int j = 0; j < methodClasses.length; j++) {
                    diff += difference(methodClasses[j], desiredParamClasses[j]);
                }
            }

            if (chosen == null || diff < lastDiff) {
                chosen = method;
                lastDiff = diff;
            }
        }

        return chosen;
    }

    /**
     * Liefert alle Felder, die in dieser Klasse oder einer ihrer Vorfahren deklariert wurden.
     *
     * @param clazz Klasse deren Felder zu suchen sind.
     * @return Alle Felder aller Klassen
     */
    public static List<Field> getAllFields(final Class clazz)
    {
        final List<Field> fields = new LinkedList<Field>();
        final List<Class> classes = new LinkedList<Class>();

        classes.add(clazz);
        classes.addAll(Objects.asList(getSuperClasses(clazz)));

        for (Class cls : classes)
        {
            fields.addAll(Objects.asList(cls.getDeclaredFields()));
        }

        return Collections.unmodifiableList(fields);
    }

    /**
     * Gibt die Klassen der Instanzen zurück.
     */
    public static Class[] getClasses(Object[] instances)
    {
        if (Objects.isEmpty(instances))
        {
            return instances == null ? null : EMPTY_CLASS_ARRAY;
        }

        final Class[] classes = new Class[instances.length];
        for (int i = 0; i < classes.length; i++)
        {
            final Object arg = instances[i];
            classes[i] = arg != null ? arg.getClass() : null;
        }

        return classes;
    }

    /**
     * Liefert das erste Feld, dieser Klasse oder einer ihrer Vorfahren, das dem angegebenen Namen entspricht.
     *
     * @param clazz Klasse deren Felder zu durchsuchen sind.
     * @param name Name des Feldes.
     * @return Ein Feld oder <code>null</code>.
     */
    public static Field getField(final Class clazz, final String name)
    {
        if (clazz == null || name == null)
        {
            return null;
        }

        for (Field field : getAllFields(clazz))
        {
            if (name.equals(field.getName()))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Liefert den Wert des ersten Feldes, dieses Objektes, das dem angegebenen Namen entspricht.
     *
     * @param object Object dessen Feld zu suchen ist.
     * @param name Name des Feldes.
     * @return Der Wert oder <code>null</code>, wenn nichts gefunden wurde.
     */
    public static Object getFieldValue(Object object, String name)
    {
        try
        {
            final Field field = getField(object.getClass(), name);
            field.setAccessible(true);
            return field.get(object);
        }
        catch (Throwable throwable)
        {
            return null;
        }
    }

    /**
     * Liefert den Typ des Parameters am angegebenen Index oder <code>null</code>, wenn der Index nicht belegt ist.
     */
    public static Class getParamType(final Method method, final int index)
    {
        final int paramCount = paramCount(method);
        if (index < 0 || index >= paramCount)
        {
            throw new IndexOutOfBoundsException("Index: " + index + ", ParamCount: " + paramCount);
        }
        return method.getParameterTypes()[index];
    }

    /**
     * Liefert den Klassennamen ohne package.
     */
    public static String getShortClassName(Class clazz)
    {
        final String name = clazz.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Liefert die speziellste gemeinsame Klasse.
     * Dabei kann ausgeschlossen oder erlaubt werden, dass ein Interface zurückgegeben wird.
     */
    public static Class getSuperClass(final Class classA, final Class classB, final boolean allowInterfaces)
    {
        if (classA == null)
        {
            return classB == null ? Object.class : classB;
        }
        else if (classB == null)
        {
            return classA;
        }

        if (classA.equals(classB))
        {
            return classA;
        }

        final List<Class> classesA = new ArrayList<Class>();
        classesA.add(classA);

        for (Class clazz : getSuperClasses(classA))
        {
            if (allowInterfaces || !clazz.isInterface())
            {
                classesA.add(clazz);
            }
        }

        final Set<Class> classesB = new HashSet<>();
        classesB.add(classB);
        classesB.addAll(asSet(getSuperClasses(classB)));

        classesA.retainAll(classesB);
        final Class first = Objects.first(classesA);
        return first == null ? Object.class : first;
    }

    /**
     * Liest alle Klassen und Interfaces denen diese Klasse angehört.
     * Angefangen wird bei den Interfaces, dann folgen Ebenenweise deren Parents
     * und dann gehts mit der Superklasse der angegebenen Klasse weiter. Der
     * letzte Teil ist rekursiv implementiert.
     */
    public static Class[] getSuperClasses(final Class clazz)
    {
        final List<Class> result = new ArrayList<Class>();

        result.add(clazz);

        // Einlesen aller Interfaces und ihrer Vorfahren
        int i = 0;
        while (i < result.size())
        {
            final Class nextClass = result.get(i);
            if (nextClass != null)
            {
                result.addAll(Objects.asList(nextClass.getInterfaces()));
            }
            i++;
        }

        // Weiter mit der Superklasse
        final Class superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            result.add(superClass);
            result.addAll(Objects.asList(getSuperClasses(superClass)));
        }

        // Eindeutigkeit herstellen
        final Set<Class> unique = new LinkedHashSet<Class>(result);
        unique.remove(clazz);
        return unique.toArray(new Class[unique.size()]);
    }

    /**
     * Stellt fest, ob und an welcher Stelle der Methodenname den angegebenen Text enthält.
     * NullPointer führen zu -1.
     */
    public static int indexOf(final Method method, final String string)
    {
        if (method == null || string == null)
        {
            return -1;
        }
        return method.getName().indexOf(string);
    }

    /**
     * Liefert die Anzahl der Parameter der Methode.
     * NullPointer führt zu -1.
     */
    public static int paramCount(final Method method)
    {
        if (method == null)
        {
            return -1;
        }
        final Class[] parameterTypes = method.getParameterTypes();
        return Objects.isEmpty(parameterTypes) ? 0 : parameterTypes.length;
    }

    /**
     * Stellt fest, ob der Methodenname mit einem der angegebenen Texte beginnt.
     * NullPointer führen zu <code>false</code>.
     */
    public static boolean startsWith(final Method method, final String... strings)
    {
        if (method == null || Objects.isEmpty(strings))
        {
            return false;
        }

        for (final String string : strings)
        {
            if (method.getName().startsWith(string))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Diese Methode stellt sicher, dass der angegebene Methodenname in der genannten Instanz auch existiert.
     * Sollte dies nicht der Fall sein, wird ein Laufzeitfehler ausgegeben, der auf den unbekannten bzw.
     * fehlerhaften Methodennamen hinweist.
     *
     * Diese Methode stellt ein Stück mehr an Sicherheit beim Umgang mit DynamicProxys dar, so dass u.a.
     * Schreibfehler leichter gefunden werden können und DynamicProxys seltener vergessen werden.
     *
     * @param methodName         Der Name der Methode.
     * @param associatedInstance Die Instanz in der die Methode existieren soll.
     * @return Gibt den Namen der Methode zurück, insofern diese in der Instanz gefunden werden konnte.
     * @throws IllegalArgumentException Wenn der Methodenname in der Instanz unbekannt ist.
     */
    public static String validateMethodName(final String methodName, final Object associatedInstance)
    {
        Objects.checkParam(methodName, "methodName");
        Objects.checkParam(associatedInstance, "associatedInstance");

        return validateMethodName(methodName, associatedInstance.getClass());
    }

    /**
     * Diese Methode stellt sicher, dass der angegebene Methodenname in der genannten Klasse auch existiert.
     * Sollte dies nicht der Fall sein, wird ein Laufzeitfehler ausgegeben, der auf den unbekannten bzw.
     * fehlerhaften Methodennamen hinweist.
     *
     * Diese Methode stellt ein Stück mehr an Sicherheit beim Umgang mit DynamicProxys dar, so dass u.a.
     * Schreibfehler leichter gefunden werden können und DynamicProxys seltener vergessen werden.
     *
     * @param methodName      Der Name der Methode.
     * @param associatedClass Die Klasse in der die Methode existieren soll.
     * @return Gibt den Namen der Methode zurück, insofern diese in der Klasse gefunden werden konnte.
     * @throws IllegalArgumentException Wenn der Methodenname in der Klasse unbekannt ist.
     */
    public static String validateMethodName(final String methodName, final Class associatedClass)
    {
        Objects.checkParam(methodName, "methodName");
        Objects.checkParam(associatedClass, "associatedClass");

        final Method[] methods = associatedClass.getMethods();
        for (final Method method : methods) {
            // Wenn die Methode gefunden werden konnte, ...
            if (methodName.equals(method.getName())) {
                // ... dann ist sie auch gültig und wird zurückgegeben.
                return methodName;
            }
        }

        // Wenn die Methode nicht gefunden wurde, dann gibts hier die passende Exception.
        throw new IllegalArgumentException("Unable to find method '" + methodName + "' in class '"
                + associatedClass.getName() + "'.");
    }

    /**
     * Interface für einen InvocationHandler, der Serialisiert werden kann.
     */
    public interface SerializableInvocationHandler extends InvocationHandler, Serializable {}
}
