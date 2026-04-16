package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class NewDisposerCreator implements SyntheticBeanCreator<DisposableResult> {
    @Override
    public DisposableResult create(SyntheticInjections injections, Parameters params) {
        return new DisposableResult();
    }
}
