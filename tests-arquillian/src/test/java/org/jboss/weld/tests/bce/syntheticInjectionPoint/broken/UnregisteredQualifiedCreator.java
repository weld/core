package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

import org.jboss.weld.tests.bce.syntheticInjectionPoint.basic.MyQualifier;

/**
 * Attempts get(Class, qualifier) — ExistingDefaultBean exists without @MyQualifier,
 * but the injection point was not registered with that qualifier.
 */
public class UnregisteredQualifiedCreator implements SyntheticBeanCreator<UnregisteredQualifiedResult> {
    @Override
    public UnregisteredQualifiedResult create(SyntheticInjections injections, Parameters params) {
        injections.get(ExistingDefaultBean.class, MyQualifier.Literal.INSTANCE);
        return new UnregisteredQualifiedResult();
    }
}
