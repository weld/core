package org.jboss.weld.tests.invokable.lookup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// deliberately not a qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAQualifier {
}
