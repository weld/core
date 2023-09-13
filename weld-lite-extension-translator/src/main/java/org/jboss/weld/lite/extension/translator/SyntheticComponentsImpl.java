package org.jboss.weld.lite.extension.translator;

import java.util.List;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserverBuilder;
import jakarta.enterprise.lang.model.types.Type;

class SyntheticComponentsImpl implements SyntheticComponents {
    final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    final List<SyntheticObserverBuilderImpl<?>> syntheticObservers;
    final Class<? extends BuildCompatibleExtension> extensionClass;

    SyntheticComponentsImpl(List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl<?>> syntheticObservers,
            Class<? extends BuildCompatibleExtension> extensionClass) {
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
        this.extensionClass = extensionClass;
    }

    @Override
    public <T> SyntheticBeanBuilder<T> addBean(Class<T> implementationClass) {
        SyntheticBeanBuilderImpl<T> builder = new SyntheticBeanBuilderImpl<T>(implementationClass, extensionClass);
        syntheticBeans.add(builder);
        return builder;
    }

    @Override
    public <T> SyntheticObserverBuilder<T> addObserver(Class<T> eventType) {
        SyntheticObserverBuilderImpl<T> builder = new SyntheticObserverBuilderImpl<>(extensionClass, eventType);
        syntheticObservers.add(builder);
        return builder;
    }

    @Override
    public <T> SyntheticObserverBuilder<T> addObserver(Type eventType) {
        SyntheticObserverBuilderImpl<T> builder = new SyntheticObserverBuilderImpl<>(extensionClass,
                ((TypeImpl<?>) eventType).reflection.getType());
        syntheticObservers.add(builder);
        return builder;
    }
}
