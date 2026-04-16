package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

/**
 * Registers synthetic beans whose creators try to look up beans that were
 * NOT registered via withInjectionPoint(). Each lookup should throw
 * IllegalArgumentException per the spec.
 */
public class UnregisteredLookupExtension implements BuildCompatibleExtension {
    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(UnregisteredClassResult.class)
                .type(UnregisteredClassResult.class)
                .scope(Dependent.class)
                .createWith(UnregisteredLookupCreator.class);

        syn.addBean(UnregisteredTypeLiteralResult.class)
                .type(UnregisteredTypeLiteralResult.class)
                .scope(Dependent.class)
                .createWith(UnregisteredTypeLiteralCreator.class);

        syn.addBean(UnregisteredQualifiedResult.class)
                .type(UnregisteredQualifiedResult.class)
                .scope(Dependent.class)
                .createWith(UnregisteredQualifiedCreator.class);
    }
}
