package org.jboss.weld.tests.eager.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;

/**
 * A stereotype that inherits @Eager from ParentEagerStereotype.
 * Does NOT declare @Eager itself.
 */
@Stereotype
@ParentEagerStereotype
@ApplicationScoped
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChildStereotype {
}
