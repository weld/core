package org.jboss.weld.environment.servlet.test.provider;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

@Vetoed
public class MyCDIProvider implements CDIProvider {

    public static boolean isCalled = false;

    private CDI<Object> cdi;

    public MyCDIProvider(CDI<Object> cdi) {
        this.cdi = cdi;
    }

    @Override
    public CDI<Object> getCDI() {
        isCalled = true;
        return cdi;
    }

    public static void reset() {
        isCalled = false;
    }
}
