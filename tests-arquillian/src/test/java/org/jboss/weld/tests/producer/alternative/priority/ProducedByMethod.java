package org.jboss.weld.tests.producer.alternative.priority;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ProducedByMethod {
}
