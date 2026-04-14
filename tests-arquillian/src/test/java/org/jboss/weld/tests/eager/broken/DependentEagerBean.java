package org.jboss.weld.tests.eager.broken;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.Eager;

@Dependent
@Eager
public class DependentEagerBean {
}
