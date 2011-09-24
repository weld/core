package org.jboss.weld.metadata;

import org.jboss.weld.bootstrap.spi.Metadata;

public class MetadataImpl<T> implements Metadata<T> {

    private final String location;
    private final T value;

    public MetadataImpl(T value, String location) {
        this.location = location;
        this.value = value;
    }

    public String getLocation() {
        return location;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getLocation();
    }

}
