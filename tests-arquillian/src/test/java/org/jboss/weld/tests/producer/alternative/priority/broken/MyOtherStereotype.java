package org.jboss.weld.tests.producer.alternative.priority.broken;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Stereotype;

@Stereotype
@Retention(RetentionPolicy.RUNTIME)
@Priority(20)
public @interface MyOtherStereotype {
}
