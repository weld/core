package org.jboss.weld.tests.injectionPoint.resource.extension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

@ApplicationScoped
@Specializes
public class FooSpecialized extends Foo {
}
