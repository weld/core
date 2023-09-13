package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

@Qualifier
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface Baz {

}
