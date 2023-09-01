package org.jboss.weld.tests.invokable.lookup;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// no bean with this qualifier is supposed to exist
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MyQualifier3 {
}
