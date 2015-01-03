package org.jboss.weld.tests.weld1834;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 */
@ApplicationScoped
public class Bar extends Foo {

    public Bar() {
        super("Bar");
    }
}
