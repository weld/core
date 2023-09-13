package org.jboss.weld.tests.contexts.dependent;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
@InterceptorBinding
public @interface Transactional {

}
