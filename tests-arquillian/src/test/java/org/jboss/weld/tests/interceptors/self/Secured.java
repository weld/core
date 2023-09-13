package org.jboss.weld.tests.interceptors.self;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/**
 * @author Marius Bogoevici
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Secured {
}
