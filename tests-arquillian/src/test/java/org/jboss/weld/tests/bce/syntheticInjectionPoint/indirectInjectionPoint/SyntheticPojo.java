package org.jboss.weld.tests.bce.syntheticInjectionPoint.indirectInjectionPoint;

import jakarta.enterprise.inject.spi.InjectionPoint;

public class SyntheticPojo {
    public final InjectionPoint capturedInjectionPoint;

    public SyntheticPojo(InjectionPoint capturedInjectionPoint) {
        this.capturedInjectionPoint = capturedInjectionPoint;
    }
}
