package org.jboss.weld.tests.unit.threadlocal;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

public class Bar {

    @Inject
    InjectionPoint ip;

    public void ping() {
        ip.getType();
    }

}
