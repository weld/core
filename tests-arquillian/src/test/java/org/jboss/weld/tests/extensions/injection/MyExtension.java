package org.jboss.weld.tests.extensions.injection;

import javax.enterprise.inject.spi.Extension;

/**
 *
 */
public class MyExtension implements Extension {

    public String foo;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }
}
