package org.jboss.weld.tests.unit.threadlocal;

import jakarta.inject.Inject;

public class Baz {

    @Inject
    Bar bar;

    public Bar getBar() {
        return bar;
    }

}
