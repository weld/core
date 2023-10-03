package org.jboss.weld.tests.contexts.retrieval;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.NormalScope;

@NormalScope
@Inherited
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface CustomScope {
}
