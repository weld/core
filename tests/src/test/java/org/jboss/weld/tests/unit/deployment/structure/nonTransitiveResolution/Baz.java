package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface Baz {

}
