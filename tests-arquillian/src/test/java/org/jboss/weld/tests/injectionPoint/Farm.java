package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@Dependent
public class Farm implements Serializable {

    @Inject
    private InjectionPoint injectionPoint;

    public void ping() {
    }

    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

}
