package de.teilecafe.tools;

/**
 * Definiert einen Wert, der nur die angegebene Zeit gültig ist und dann verfällt.
 *
 * @param <T> Typ des Wertes.
 * @author Bob Tehl
 */
public class FleetingValue<T> {
    private T value;
    private long validUntil;

    public FleetingValue() {
        this(null, 0);
    }

    public FleetingValue(final T value, final int timeToLive) {
        set(value, timeToLive);
    }

    /**
     * Setzt den Wert und seine Haltbarkeit.
     *
     * @param value         Wert.
     * @param timeToLive    Haltbarkeit in ms.
     */
    public final void set(final T value, final int timeToLive) {
        this.value = value;
        this.validUntil = System.currentTimeMillis() + timeToLive;
    }

    /**
     * Prüft, ob der Wert noch gültig ist.
     * @return <code>true</code> = gültig, sonst <code>false</code>.
     */
    public final boolean isValid() {
        return System.currentTimeMillis() < validUntil;
    }

    /**
     * Liefert den Wert, wenn er noch gültig ist.
     * @return Der Wert, wenn er noch gültig ist, sonst <code>null</code>.
     */
    public final T get() {
        return isValid() ? value : null;
    }
}
