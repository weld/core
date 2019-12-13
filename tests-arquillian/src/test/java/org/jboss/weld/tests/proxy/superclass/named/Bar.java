package org.jboss.weld.tests.proxy.superclass.named;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

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
