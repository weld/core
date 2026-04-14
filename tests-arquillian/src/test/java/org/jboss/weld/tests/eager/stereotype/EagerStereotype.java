package org.jboss.weld.tests.eager.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Eager;
import jakarta.enterprise.inject.Stereotype;

@Stereotype
@ApplicationScoped
@Eager
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EagerStereotype {
}
