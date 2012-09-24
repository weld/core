package org.jboss.weld.environment.se.test.instance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * @author Mark Proctor
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Qualifier
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface KPQ {
}
