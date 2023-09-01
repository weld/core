package org.jboss.weld.tests.invokable.lookup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// an extension turns this annotation into a CDI qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ToBeQualifier {
}
