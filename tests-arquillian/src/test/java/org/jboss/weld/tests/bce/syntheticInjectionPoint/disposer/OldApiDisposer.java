package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;

public class OldApiDisposer implements SyntheticBeanDisposer<DisposableResult> {
    @Override
    public void dispose(DisposableResult instance, Instance<Object> lookup, Parameters params) {
        DisposableResult.disposed = true;
    }
}
