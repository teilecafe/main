package de.teilecafe.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Definiert einen Wert, der Lazy erzeugt wird, nach einer Zeit wieder verfällt,
 * um dann bei Bedarf wieder neu erzeugt zu werden.
 *
 * @author Bob Tehl
 */
public final class PhoenixValue<T> {
    private final FleetingValue<T> value;
    private final int timeToLive;
    private final Ash<T> ash;

    public static final Fire FIRE = new Fire();

    /**
     * Konstruktor.
     *
     * @param timeToLive  Lebenszeit des Wertes in Millisekunden.
     * @param ash         Implementierung zur Erzeugung eines Wertes.
     */
    public PhoenixValue(final int timeToLive, final Ash<T> ash) {
        super();
        this.value = new FleetingValue<>();
        this.timeToLive = timeToLive;
        this.ash = ash;

        FIRE.add(this);
    }

    public PhoenixValue(final Ash<T> ash) {
        this(Integer.MAX_VALUE, ash);
    }

    /**
     * Liefert den Wert zurück, wenn er noch gültig ist,
     * andernfalls wird ein neuer Wert abgerufen und dieser zurückgegeben.
     */
    public final T get() {
        T val = value.get();
        if (val == null) {
            val = ash.rebirth();
            value.set(val, timeToLive);
        }
        return val;
    }

    public final void burn() {
        value.set(null, timeToLive);
    }

    /**
     * Erzeugt den Wert neu, wenn er verfallen war.
     *
     * Sage vom ersten Phönix: "Aus den Flammen und der Asche steigt der Phönix erneut empor,
     * wiedergeboren und strahlend schön und jung."
     *
     * @param <T> Typ des Werts.
     */
    public interface Ash<T> {

        /**
         * Erzeugt den Wert neu, wenn er verfallen war.
         *
         * Sage vom ersten Phönix: "Aus den Flammen und der Asche steigt der Phönix erneut empor,
         * wiedergeboren und strahlend schön und jung."
         */
        T rebirth();
    }

    /**
     *
     */
    public static final class Fire {
        private final List<WeakReference<PhoenixValue>> phoenixValues = Collections.synchronizedList(new ArrayList<>());

        public final void add(final PhoenixValue phoenixValue) {
            phoenixValues.add(new WeakReference<>(phoenixValue));
        }

        public final void burnAll() {
            final Set<WeakReference> dead = new HashSet<>();
            for (final WeakReference<PhoenixValue> weakRef : new ArrayList<>(phoenixValues)) {
                final PhoenixValue phoenixValue = weakRef == null ? null : weakRef.get();
                if (phoenixValue == null) {
                    if (weakRef != null) {
                        dead.add(weakRef);
                    }
                } else {
                    phoenixValue.burn();
                }
            }
            phoenixValues.removeAll(dead);
        }
    }
}
