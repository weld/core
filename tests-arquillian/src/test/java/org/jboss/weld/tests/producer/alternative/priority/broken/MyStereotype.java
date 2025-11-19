package org.jboss.weld.tests.producer.alternative.priority.broken;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Stereotype;

@Retention(RetentionPolicy.RUNTIME)
@Stereotype
@Priority(10)
public @interface MyStereotype {
}
