package io.tava.config;

import com.typesafe.config.*;
import io.tava.function.Function1;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Configuration implements Config {

    private final Config config;

    public Configuration(Config config) {
        this.config = config;
    }

    @Override
    public ConfigObject root() {
        return this.config.root();
    }

    @Override
    public ConfigOrigin origin() {
        return this.config.origin();
    }

    @Override
    public Config withFallback(ConfigMergeable other) {
        return this.config.withFallback(other);
    }

    @Override
    public Config resolve() {
        return this.config.resolve();
    }

    @Override
    public Config resolve(ConfigResolveOptions options) {
        return this.config.resolve(options);
    }

    @Override
    public boolean isResolved() {
        return this.config.isResolved();
    }

    @Override
    public Config resolveWith(Config source) {
        return this.config.resolveWith(source);
    }

    @Override
    public Config resolveWith(Config source, ConfigResolveOptions options) {
        return this.config.resolveWith(source, options);
    }

    @Override
    public void checkValid(Config reference, String... restrictToPaths) {
        this.config.checkValid(reference, restrictToPaths);
    }

    @Override
    public boolean hasPath(String path) {
        return this.config.hasPath(path);
    }

    @Override
    public boolean hasPathOrNull(String path) {
        return this.config.hasPathOrNull(path);
    }

    @Override
    public boolean isEmpty() {
        return this.config.isEmpty();
    }

    @Override
    public Set<Map.Entry<String, ConfigValue>> entrySet() {
        return this.config.entrySet();
    }

    @Override
    public boolean getIsNull(String path) {
        return this.config.getIsNull(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public boolean getBoolean(String path, Function1<String, Boolean> defaultValueFunction) {
        return get(path, this::getBoolean, defaultValueFunction, false);
    }


    public boolean getBoolean(String path, boolean defaultValue) {
        return get(path, this::getBoolean, null, defaultValue);
    }


    @Override
    public Number getNumber(String path) {
        return this.config.getNumber(path);
    }

    public Number getNumber(String path, Function1<String, Number> defaultValueFunction) {
        return get(path, this::getNumber, defaultValueFunction, null);
    }

    public Number getNumber(String path, Number defaultValue) {
        return get(path, this::getNumber, null, defaultValue);
    }

    @Override
    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public int getInt(String path, Function1<String, Integer> defaultValueFunction) {
        return get(path, this::getInt, defaultValueFunction, 0);
    }

    public int getInt(String path, int defaultValue) {
        return get(path, this::getInt, null, defaultValue);
    }

    @Override
    public long getLong(String path) {
        return this.config.getLong(path);
    }


    public long getLong(String path, Function1<String, Long> defaultValueFunction) {
        return get(path, this::getLong, defaultValueFunction, 0L);

    }

    public long getLong(String path, long defaultValue) {
        return get(path, this::getLong, null, defaultValue);

    }

    @Override
    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public double getDouble(String path, Function1<String, Double> defaultValueFunction) {
        return get(path, this::getDouble, defaultValueFunction, 0D);
    }

    public double getDouble(String path, double defaultValue) {
        return get(path, this::getDouble, null, defaultValue);
    }

    @Override
    public String getString(String path) {
        return this.config.getString(path);
    }

    public String getString(String path, Function1<String, String> defaultValueFunction) {
        return get(path, this::getString, defaultValueFunction, null);
    }

    public String getString(String path, String defaultValue) {
        return get(path, this::getString, null, defaultValue);
    }


    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path) {
        return this.config.getEnum(enumClass, path);
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path, Function1<String, T> defaultValueFunction) {
        return get(path, key -> this.getEnum(enumClass, key), defaultValueFunction, null);
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path, T defaultValue) {
        return get(path, key -> this.getEnum(enumClass, key), null, defaultValue);
    }

    @Override
    public ConfigObject getObject(String path) {
        return this.config.getObject(path);
    }

    @Override
    public Configuration getConfig(String path) {
        return new Configuration(this.config.getConfig(path));
    }

    @Override
    public Object getAnyRef(String path) {
        return this.config.getAnyRef(path);
    }

    public Object getAnyRef(String path, Function1<String, Object> defaultValueFunction) {
        return get(path, this::getAnyRef, defaultValueFunction, null);
    }

    public Object getAnyRef(String path, Object defaultValue) {
        return get(path, this::getAnyRef, null, defaultValue);
    }


    @Override
    public ConfigValue getValue(String path) {
        return this.config.getValue(path);
    }

    @Override
    public Long getBytes(String path) {
        return this.config.getBytes(path);
    }

    public Long getBytes(String path, Function1<String, Long> defaultValueFunction) {
        return get(path, this::getBytes, defaultValueFunction, null);
    }

    public Long getBytes(String path, Long defaultValue) {
        return get(path, this::getBytes, null, defaultValue);
    }

    @Override
    public ConfigMemorySize getMemorySize(String path) {
        return this.config.getMemorySize(path);
    }

    @Override
    @Deprecated
    public Long getMilliseconds(String path) {
        return this.config.getMilliseconds(path);
    }

    @Override
    @Deprecated
    public Long getNanoseconds(String path) {
        return this.config.getNanoseconds(path);
    }

    @Override
    public long getDuration(String path, TimeUnit unit) {
        return this.config.getDuration(path, unit);
    }

    public long getDuration(String path, TimeUnit unit, Function1<String, Long> defaultValueFunction) {
        return get(path, key -> this.getDuration(key, unit), defaultValueFunction, null);
    }

    public long getDuration(String path, TimeUnit unit, long defaultValue) {
        return get(path, key -> this.getDuration(key, unit), null, defaultValue);
    }

    @Override
    public Duration getDuration(String path) {
        return this.config.getDuration(path);
    }

    public Duration getDuration(String path, Function1<String, Duration> defaultValueFunction) {
        return get(path, this::getDuration, defaultValueFunction, null);
    }

    public Duration getDuration(String path, Duration defaultValue) {
        return get(path, this::getDuration, null, defaultValue);
    }

    @Override
    public Period getPeriod(String path) {
        return this.config.getPeriod(path);
    }

    public Period getPeriod(String path, Function1<String, Period> defaultValueFunction) {
        return get(path, this::getPeriod, defaultValueFunction, null);
    }

    public Period getPeriod(String path, Period defaultValue) {
        return get(path, this::getPeriod, null, defaultValue);
    }

    @Override
    public TemporalAmount getTemporal(String path) {
        return this.config.getTemporal(path);
    }

    public TemporalAmount getTemporal(String path, Function1<String, TemporalAmount> defaultValueFunction) {
        return get(path, this::getTemporal, defaultValueFunction, null);
    }

    public TemporalAmount getTemporal(String path, TemporalAmount defaultValue) {
        return get(path, this::getTemporal, null, defaultValue);
    }

    @Override
    public ConfigList getList(String path) {
        return this.config.getList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return this.config.getBooleanList(path);
    }

    public List<Boolean> getBooleanList(String path, Function1<String, List<Boolean>> defaultValueFunction) {
        return get(path, this::getBooleanList, defaultValueFunction, null);
    }

    public List<Boolean> getBooleanList(String path, List<Boolean> defaultValue) {
        return get(path, this::getBooleanList, null, defaultValue);
    }

    @Override
    public List<Number> getNumberList(String path) {
        return this.config.getNumberList(path);
    }

    public List<Number> getNumberList(String path, Function1<String, List<Number>> defaultValueFunction) {
        return get(path, this::getNumberList, defaultValueFunction, null);
    }

    public List<Number> getNumberList(String path, List<Number> defaultValue) {
        return get(path, this::getNumberList, null, defaultValue);
    }

    @Override
    public List<Integer> getIntList(String path) {
        return this.config.getIntList(path);
    }

    public List<Integer> getIntList(String path, Function1<String, List<Integer>> defaultValueFunction) {
        return get(path, this::getIntList, defaultValueFunction, null);
    }

    public List<Integer> getIntList(String path, List<Integer> defaultValue) {
        return get(path, this::getIntList, null, defaultValue);
    }


    @Override
    public List<Long> getLongList(String path) {
        return this.config.getLongList(path);
    }

    public List<Long> getLongList(String path, Function1<String, List<Long>> defaultValueFunction) {
        return get(path, this::getLongList, defaultValueFunction, null);
    }

    public List<Long> getLongList(String path, List<Long> defaultValue) {
        return get(path, this::getLongList, null, defaultValue);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return this.config.getDoubleList(path);
    }

    public List<Double> getDoubleList(String path, Function1<String, List<Double>> defaultValueFunction) {
        return get(path, this::getDoubleList, defaultValueFunction, null);
    }

    public List<Double> getDoubleList(String path, List<Double> defaultValue) {
        return get(path, this::getDoubleList, null, defaultValue);
    }

    @Override
    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public List<String> getStringList(String path, Function1<String, List<String>> defaultValueFunction) {
        return get(path, this::getStringList, defaultValueFunction, null);
    }

    public List<String> getStringList(String path, List<String> defaultValue) {
        return get(path, this::getStringList, null, defaultValue);
    }

    @Override
    public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path) {
        return this.config.getEnumList(enumClass, path);
    }

    public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path, Function1<String, List<T>> defaultValueFunction) {
        return get(path, key -> this.getEnumList(enumClass, key), defaultValueFunction, null);
    }

    public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path, List<T> defaultValue) {
        return get(path, key -> this.getEnumList(enumClass, key), null, defaultValue);
    }

    @Override
    public List<? extends ConfigObject> getObjectList(String path) {
        return this.getObjectList(path);
    }

    @Override
    public List<Configuration> getConfigList(String path) {
        List<? extends Config> configs = this.config.getConfigList(path);
        List<Configuration> list = new ArrayList<>();
        for (Config c : configs) {
            list.add(new Configuration(c));
        }
        return list;
    }

    @Override
    public List<? extends Object> getAnyRefList(String path) {
        return this.config.getAnyRefList(path);
    }

    @Override
    public List<Long> getBytesList(String path) {
        return this.config.getBytesList(path);
    }

    public List<Long> getBytesList(String path, Function1<String, List<Long>> defaultValueFunction) {
        return get(path, this::getBytesList, defaultValueFunction, null);
    }

    public List<Long> getBytesList(String path, List<Long> defaultValue) {
        return get(path, this::getBytesList, null, defaultValue);
    }

    @Override
    public List<ConfigMemorySize> getMemorySizeList(String path) {
        return this.config.getMemorySizeList(path);
    }

    @Override
    @Deprecated
    public List<Long> getMillisecondsList(String path) {
        return this.config.getMillisecondsList(path);
    }

    @Override
    @Deprecated
    public List<Long> getNanosecondsList(String path) {
        return this.config.getNanosecondsList(path);
    }

    @Override
    public List<Long> getDurationList(String path, TimeUnit unit) {
        return this.config.getDurationList(path, unit);
    }

    public List<Long> getDurationList(String path, TimeUnit unit, Function1<String, List<Long>> defaultValueFunction) {
        return get(path, key -> this.getDurationList(key, unit), defaultValueFunction, null);
    }

    public List<Long> getDurationList(String path, TimeUnit unit, List<Long> defaultValue) {
        return get(path, key -> this.getDurationList(key, unit), null, defaultValue);
    }

    @Override
    public List<Duration> getDurationList(String path) {
        return this.config.getDurationList(path);
    }

    public List<Duration> getDurationList(String path, Function1<String, List<Duration>> defaultValueFunction) {
        return get(path, this::getDurationList, defaultValueFunction, null);
    }

    public List<Duration> getDurationList(String path, List<Duration> defaultValue) {
        return get(path, this::getDurationList, null, defaultValue);
    }

    @Override
    public Config withOnlyPath(String path) {
        return this.config.withOnlyPath(path);
    }

    @Override
    public Config withoutPath(String path) {
        return this.config.withoutPath(path);
    }

    @Override
    public Config atPath(String path) {
        return this.config.atPath(path);
    }

    @Override
    public Config atKey(String key) {
        return this.config.atKey(key);
    }

    @Override
    public Config withValue(String path, ConfigValue value) {
        return this.config.withValue(path, value);
    }

    private <R> R get(String path, Function1<String, R> get, Function1<String, R> defaultValueFunction, Object value) {
        if (this.hasPath(path)) {
            return get.apply(path);
        }
        if (defaultValueFunction != null) {
            return defaultValueFunction.apply(path);
        }
        return (R) value;
    }

}
