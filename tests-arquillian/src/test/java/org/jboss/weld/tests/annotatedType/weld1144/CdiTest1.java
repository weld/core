package org.jboss.weld.tests.annotatedType.weld1144;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Session Bean implementation class CdiTest
 */
@Dependent
public class CdiTest1 {

    @Inject
    private SomeInjectedBean someInjectedBean1;

    public SomeInjectedBean getSomeInjectedBean1() {
        return someInjectedBean1;
    }

}
