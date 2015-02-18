package org.jboss.weld.probe.integration.tests.beans;

import javax.annotation.Priority;
import javax.ejb.Stateful;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(2500)
@Stateful
public class StatefulEjbSession implements DecoratedInterface {


    @Override
    public String testMethod() {
        return "Hello from "+StatefulEjbSession.class.getSimpleName();
    }
}
