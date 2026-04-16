package org.jboss.weld.tests.eager.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Singleton;

/**
 * A stereotype that declares @Singleton scope.
 * This makes beans annotated with it discoverable in bean archives
 * with annotated discovery mode since @Singleton alone is not a
 * bean defining annotation.
 */
@Stereotype
@Singleton
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingletonStereotype {
}
