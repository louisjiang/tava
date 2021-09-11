package io.tava.serialization.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Chronology;
import org.joda.time.YearMonth;


/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 14:20
 */
public class YearMonthSerializer extends Serializer<YearMonth> {

    private static final YearMonthSerializer INSTANCE = new YearMonthSerializer();

    public static YearMonthSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, YearMonth yearMonth) {
        output.writeInt(yearMonth.getYear(), true);
        output.writeInt(yearMonth.getMonthOfYear(), true);
        output.writeString(IdentifiableChronology.idOfValue(yearMonth.getChronology()));
    }

    @Override
    public YearMonth read(Kryo kryo, Input input, Class<? extends YearMonth> type) {
        int year = input.readInt(true);
        int monthOfYear = input.readInt(true);
        Chronology chronology = IdentifiableChronology.valueOfId(input.readString());
        return new YearMonth(year, monthOfYear, chronology);
    }
}
