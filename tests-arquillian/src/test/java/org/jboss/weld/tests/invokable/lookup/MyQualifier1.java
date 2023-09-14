package org.jboss.weld.tests.invokable.lookup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MyQualifier1 {

    // non-binding
    @Nonbinding
    String value();
}
