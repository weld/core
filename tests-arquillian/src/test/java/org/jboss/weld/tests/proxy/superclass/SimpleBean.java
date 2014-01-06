package org.jboss.weld.tests.proxy.superclass;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleBean extends NotSimpleConstructorClass {

    public SimpleBean() {
        super("nothing");
    }

}
