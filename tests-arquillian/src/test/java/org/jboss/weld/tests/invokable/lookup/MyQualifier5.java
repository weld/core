package org.jboss.weld.tests.invokable.lookup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MyQualifier5 {
}
