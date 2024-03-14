package org.jboss.weld.tests.proxy.ignoreinvalidmethods.inheritance;

public abstract class AbstractSuperClass2 extends AbstractSuperClass {

    @Override
    public final void ping() {
        // this method is NOT proxyable
    }
}
