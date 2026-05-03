package org.jboss.weld.tests.autoclose.basic;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.inject.Stereotype;

@Stereotype
@AutoClose
@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoCloseStereotype {
}
