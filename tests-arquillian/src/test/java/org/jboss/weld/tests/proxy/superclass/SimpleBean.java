package org.jboss.weld.tests.proxy.superclass;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleBean extends NotSimpleConstructorClass {

    public SimpleBean() {
        super("nothing");
    }

}
