package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

import org.jboss.weld.tests.bce.syntheticInjectionPoint.basic.MyQualifier;

/**
 * ExistingDefaultBean exists with @Default but the injection
 * point requires @MyQualifier — no matching bean with that qualifier.
 */
public class WrongQualifierExtension implements BuildCompatibleExtension {
    @Synthesis
    public void register(SyntheticComponents syn) {
        syn.addBean(String.class)
                .type(String.class)
                .scope(ApplicationScoped.class)
                .withInjectionPoint(ExistingDefaultBean.class, MyQualifier.Literal.INSTANCE)
                .createWith(DummyCreator.class);
    }
}
