package org.jboss.weld.manager;

import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.util.Function;

public class RemoveMetadataWrapperFunction<T> implements Function<Metadata<T>, T> {

    public T apply(Metadata<T> from) {
        return from.getValue();
    }

}
