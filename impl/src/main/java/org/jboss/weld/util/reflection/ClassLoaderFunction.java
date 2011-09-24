package org.jboss.weld.util.reflection;

import com.google.common.base.Function;
import org.jboss.weld.resources.spi.ResourceLoader;

public class ClassLoaderFunction implements Function<String, Class<?>> {

    private final ResourceLoader resourceLoader;

    public ClassLoaderFunction(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Class<?> apply(String from) {
        return resourceLoader.classForName(from);
    }

}