package org.jboss.weld.tests.builtinBeans.weld471;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.inject.Inject;

@ApplicationScoped
public class Bar {
	@Inject @New private Instance<Foo> foo;
}
