package org.jboss.weld.tests.annotatedType.weld1144;

import javax.inject.Inject;

public class CdiTest2 extends CdiTest1 {

	@Inject
	private SomeInjectedBean someInjectedBean2;

    public SomeInjectedBean getSomeInjectedBean2() {
        return someInjectedBean2;
    }

}
