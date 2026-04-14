package org.jboss.weld.tests.injectionPoint.notInjectedInstance;

import jakarta.enterprise.context.Dependent;

@Nice
@Dependent
public class NiceFoo extends Foo {
}
