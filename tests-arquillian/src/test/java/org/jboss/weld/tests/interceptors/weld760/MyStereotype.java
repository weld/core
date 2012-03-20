package org.jboss.weld.tests.interceptors.weld760;

import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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