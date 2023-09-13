package org.jboss.weld.tests.interceptors.weld760;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Named;

/**
 *
 */
@Stereotype
@Named
@MyInterceptorBinding
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyStereotype {
}
