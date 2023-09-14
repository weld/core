package org.jboss.weld.tests.invokable.lookup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.inject.Qualifier;

// no bean with this qualifier is supposed to exist
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MyQualifier3 {
}
