package org.jboss.weld.probe.tests.integration.deployment.sessions;

import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Alternative;

@Stateless
@Alternative
public class StatelessEjbSession implements DecoratedInterface {

    @Override
    public String testMethod() {
        return "Hello from "+StatelessEjbSession.class.getSimpleName();
    }

}
