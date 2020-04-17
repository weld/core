package org.jboss.weld.tests.annotations.weld1131;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;

/**
 *
 */
@SuppressWarnings("serial")
@RequestScoped
@MyAnnotation
public class Foo implements Serializable {

    public Foo() {
    }

    @MyAnnotation
    public String getBar() {
        return "bar";
    }
}
