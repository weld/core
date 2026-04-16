package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;
import jakarta.enterprise.util.TypeLiteral;

/**
 * Implementation of {@link SyntheticInjections} that validates lookups
 * against registered injection points. Per the spec, lookups for
 * type/qualifier combinations not registered via
 * {@code SyntheticBeanBuilder.withInjectionPoint()} throw
 * {@link IllegalArgumentException}.
 * <p>
 * Injection points are also validated at deployment time to catch
 * unsatisfied dependencies early.
 */
class SyntheticInjectionsImpl implements SyntheticInjections {

    private final Instance<Object> lookup;
    private final List<SyntheticBeanBuilderImpl.InjectionPointDeclaration> registeredInjectionPoints;

    SyntheticInjectionsImpl(Instance<Object> lookup,
            List<SyntheticBeanBuilderImpl.InjectionPointDeclaration> registeredInjectionPoints) {
        this.lookup = lookup;
        this.registeredInjectionPoints = registeredInjectionPoints;
    }

    @Override
    public <T> T get(Class<T> type, Annotation... qualifiers) {
        validateRegistered(type, qualifiers);
        return lookup.select(type, qualifiers).get();
    }

    @Override
    public <T> T get(TypeLiteral<T> type, Annotation... qualifiers) {
        validateRegistered(type.getType(), qualifiers);
        return lookup.select(type, qualifiers).get();
    }

    private void validateRegistered(Type requestedType, Annotation... requestedQualifiers) {
        Set<Annotation> requested = new HashSet<>(Arrays.asList(requestedQualifiers));
        if (requested.isEmpty()) {
            requested.add(Default.Literal.INSTANCE);
        }

        for (SyntheticBeanBuilderImpl.InjectionPointDeclaration ip : registeredInjectionPoints) {
            Set<Annotation> registered = ip.qualifiers.isEmpty()
                    ? Set.of(Default.Literal.INSTANCE)
                    : ip.qualifiers;
            if (ip.type.equals(requestedType) && registered.equals(requested)) {
                return;
            }
        }

        throw new IllegalArgumentException("No injection point registered for type " + requestedType
                + " with qualifiers " + requested
                + ". Use SyntheticBeanBuilder.withInjectionPoint() to register it.");
    }
}
