package org.jboss.weld.tests.annotatedType.weld1144;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class CdiTest2 extends CdiTest1 {

	@Inject
	private SomeInjectedBean someInjectedBean2;

    public SomeInjectedBean getSomeInjectedBean2() {
        return someInjectedBean2;
    }

}
