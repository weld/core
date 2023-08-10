package org.jboss.weld.tests.invokable.metadata.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is made {@code @Invokable} via an extension
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DefinitelyNotInvokable {
}
