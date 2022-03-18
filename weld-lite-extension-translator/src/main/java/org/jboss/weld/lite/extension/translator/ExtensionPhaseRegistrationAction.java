package org.jboss.weld.lite.extension.translator;

import java.util.Set;
import java.util.function.Consumer;

class ExtensionPhaseRegistrationAction {
    private final Set<Class<?>> types;
    private final Consumer<jakarta.enterprise.inject.spi.ProcessBean<?>> beanAcceptor;
    private final Consumer<jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?>> observerAcceptor;

    ExtensionPhaseRegistrationAction(Set<Class<?>> types,
            Consumer<jakarta.enterprise.inject.spi.ProcessBean<?>> beanAcceptor,
            Consumer<jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?>> observerAcceptor) {
        this.types = types;
        this.beanAcceptor = beanAcceptor;
        this.observerAcceptor = observerAcceptor;
    }

    void run(jakarta.enterprise.inject.spi.ProcessBean<?> pb) {
        if (beanAcceptor == null) {
            return;
        }

        Set<java.lang.reflect.Type> beanTypes = pb.getBean().getTypes();
        if (satisfies(beanTypes)) {
            beanAcceptor.accept(pb);
        }
    }

    void run(jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?> pom) {
        if (observerAcceptor == null) {
            return;
        }

        java.lang.reflect.Type observedType = pom.getObserverMethod().getObservedType();
        if (satisfiesObserverMethod(observedType)) {
            observerAcceptor.accept(pom);
        }
    }

    private boolean satisfies(Set<java.lang.reflect.Type> inspectedTypes) {
        for (java.lang.reflect.Type type : this.types) {
            Class<?> rawType = getRawType(type);
            if (rawType == null) {
                continue;
            }

            if (inspectedTypes.contains(rawType)) {
                return true;
            }
        }

        return false;
    }

    private boolean satisfiesObserverMethod(java.lang.reflect.Type observedType) {
        Class<?> rawObservedType = getRawType(observedType);
        if (rawObservedType == null) {
            return false;
        }
        for (java.lang.reflect.Type type : this.types) {
            Class<?> rawType = getRawType(type);
            if (rawType == null) {
                continue;
            }
            if (rawType.isAssignableFrom(rawObservedType)) {
                return true;
            }
        }

        return false;
    }

    private static Class<?> getRawType(java.lang.reflect.Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof java.lang.reflect.ParameterizedType) {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) type).getRawType();
        } else if (type instanceof java.lang.reflect.TypeVariable) {
            return getRawType(((java.lang.reflect.TypeVariable<?>) type).getBounds()[0]);
        } else if (type instanceof java.lang.reflect.WildcardType) {
            return getRawType(((java.lang.reflect.WildcardType) type).getUpperBounds()[0]);
        } else if (type instanceof java.lang.reflect.GenericArrayType) {
            Class<?> rawType = getRawType(((java.lang.reflect.GenericArrayType) type).getGenericComponentType());
            if (rawType != null) {
                return java.lang.reflect.Array.newInstance(rawType, 0).getClass();
            }
        }
        return null;
    }
}
