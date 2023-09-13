package org.jboss.weld.lite.extension.translator;

import java.util.Map;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;

class ParametersImpl implements Parameters {
    private final Map<String, Object> data;

    ParametersImpl(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return type.cast(data.get(key));
    }

    @Override
    public <T> T get(String key, Class<T> type, T defaultValue) {
        if (!data.containsKey(key)) {
            return defaultValue;
        }
        return type.cast(data.get(key));
    }
}
