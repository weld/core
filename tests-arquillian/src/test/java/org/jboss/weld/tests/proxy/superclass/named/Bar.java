package org.jboss.weld.tests.proxy.superclass.named;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * @author Yann Diorcet
 */
@ApplicationScoped
@Named("Bar1")
public class Bar extends Foo {

    public Bar() {
        super("Bar");
    }
}
