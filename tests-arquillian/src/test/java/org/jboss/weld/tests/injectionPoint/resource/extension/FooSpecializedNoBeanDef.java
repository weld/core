package org.jboss.weld.tests.injectionPoint.resource.extension;

import jakarta.enterprise.inject.Specializes;

// no bean defining annotation - this bean is not picked up via discovery but is instead registered via extension
@Specializes
public class FooSpecializedNoBeanDef extends Foo {
}
