package org.jboss.weld.tests.unit.threadlocal;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class Bar {

    @Inject
    InjectionPoint ip;

    public void ping() {
        ip.getType();
    }

}
