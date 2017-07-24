package org.jboss.weld.tests.interceptors.defaultmethod;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface FooInterceptorBinding {

}
