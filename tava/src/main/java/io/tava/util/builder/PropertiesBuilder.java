package io.tava.util.builder;

import io.tava.util.Properties;

public final class PropertiesBuilder implements MapBuilder<Object, Object, Properties> {

    private final Properties properties = new Properties();

    @Override
    public Properties build() {
        return properties;
    }
}
