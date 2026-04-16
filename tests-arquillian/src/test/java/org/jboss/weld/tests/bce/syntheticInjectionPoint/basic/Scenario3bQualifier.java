package org.jboss.weld.tests.bce.syntheticInjectionPoint.basic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface Scenario3bQualifier {
}
