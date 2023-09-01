package org.jboss.weld.tests.invokable.lookup;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MyQualifier4 {

    // binding value
    String value();
}
