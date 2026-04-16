package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

public class OldDisposerCreator implements SyntheticBeanCreator<DisposableResult> {
    @Override
    public DisposableResult create(Instance<Object> lookup, Parameters params) {
        return new DisposableResult();
    }
}
