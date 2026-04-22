package org.jboss.weld.tests.injectionPoint.beanConfigurator.indirect;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@Dependent
public class InjectionPointCaptor {
    @Inject
    InjectionPoint injectionPoint;
}
