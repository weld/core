package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticInjections;

public class NewApiDisposer implements SyntheticBeanDisposer<DisposableResult> {
    @Override
    public void dispose(DisposableResult instance, SyntheticInjections injections, Parameters params) {
        // Verify SyntheticInjections actually works during disposal
        DisposerHelper helper = injections.get(DisposerHelper.class);
        helper.cleanup();
        DisposableResult.disposed = true;
    }
}
