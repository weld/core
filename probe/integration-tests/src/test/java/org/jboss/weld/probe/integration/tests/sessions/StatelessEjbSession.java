package org.jboss.weld.probe.integration.tests.sessions;

import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;

@Stateless
@Alternative
public class StatelessEjbSession implements DecoratedInterface {

    @Override
    public String testMethod() {
        return "Hello from "+StatelessEjbSession.class.getSimpleName();
    }

}
