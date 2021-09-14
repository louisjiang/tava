package io.tava.serialization.kryo.serializer.joda;

import org.joda.time.Chronology;
import org.joda.time.chrono.*;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 12:55
 */
public enum IdentifiableChronology {

    ISO("iso", ISOChronology.getInstance()),
    COPTIC("coptic", CopticChronology.getInstance()),
    ETHIOPIC("ethiopic", EthiopicChronology.getInstance()),
    GREGORIAN("gregorian", GregorianChronology.getInstance()),
    JULIAN("julian", JulianChronology.getInstance()),
    ISLAMIC("islamic", IslamicChronology.getInstance()),
    BUDDHIST("buddhist", BuddhistChronology.getInstance()),
    GJ("gj", GJChronology.getInstance());

    private final String id;
    private final Chronology chronology;

    IdentifiableChronology(String id, Chronology chronology) {
        this.id = id;
        this.chronology = chronology;
    }


    public static String idOfValue(Chronology chronology) throws IllegalArgumentException {
        if (chronology == null) {
            return null;
        }
        Class<? extends Chronology> clazz = chronology.getClass();
        for (final IdentifiableChronology item : values()) {
            if (clazz.equals(item.chronology.getClass())) {
                return item.id;
            }
        }
        throw new IllegalArgumentException("Chronology not supported: " + clazz.getSimpleName());
    }

    public static Chronology valueOfId(final String id) throws IllegalArgumentException {
        if (id == null) {
            return null;
        }
        for (final IdentifiableChronology item : values()) {
            if (id.equals(item.id)) {
                return item.chronology;
            }
        }
        throw new IllegalArgumentException("No chronology found for id " + id);
    }

}
