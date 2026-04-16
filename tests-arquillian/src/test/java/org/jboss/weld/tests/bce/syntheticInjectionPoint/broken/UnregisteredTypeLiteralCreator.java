package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;
import jakarta.enterprise.util.TypeLiteral;

/**
 * Attempts get(TypeLiteral) for a type that exists but was not
 * registered via withInjectionPoint().
 */
public class UnregisteredTypeLiteralCreator implements SyntheticBeanCreator<UnregisteredTypeLiteralResult> {
    @Override
    public UnregisteredTypeLiteralResult create(SyntheticInjections injections, Parameters params) {
        injections.get(new TypeLiteral<ExistingDefaultBean>() {
        });
        return new UnregisteredTypeLiteralResult();
    }
}
