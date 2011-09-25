package org.jboss.weld.manager;

import com.google.common.base.Function;
import org.jboss.weld.bootstrap.spi.Metadata;

public class RemoveMetadataWrapperFunction<T> implements Function<Metadata<T>, T> {

    public T apply(Metadata<T> from) {
        return from.getValue();
    }

}
