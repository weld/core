package org.jboss.weld.tests.injectionPoint.beanConfigurator.indirect;

import jakarta.enterprise.inject.spi.InjectionPoint;

public class InjectionPointResult {
    public final InjectionPoint injectionPoint;

    public InjectionPointResult(InjectionPoint injectionPoint) {
        this.injectionPoint = injectionPoint;
    }
}
