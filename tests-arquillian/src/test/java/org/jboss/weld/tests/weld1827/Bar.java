package org.jboss.weld.tests.weld1827;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 */
@ApplicationScoped
@Named("Bar1")
public class Bar extends Foo {

    public Bar() {
        super("Bar");
    }
}
