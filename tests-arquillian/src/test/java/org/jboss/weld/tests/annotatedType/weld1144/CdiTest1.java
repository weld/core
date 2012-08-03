package org.jboss.weld.tests.annotatedType.weld1144;

import javax.inject.Inject;

/**
 * Session Bean implementation class CdiTest
 */
public class CdiTest1 {

	@Inject
	private SomeInjectedBean someInjectedBean1;

    public SomeInjectedBean getSomeInjectedBean1() {
        return someInjectedBean1;
    }

}
