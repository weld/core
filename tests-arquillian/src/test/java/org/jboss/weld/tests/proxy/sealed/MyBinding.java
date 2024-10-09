package org.jboss.weld.tests.proxy.sealed;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

@InterceptorBinding
@Inherited
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface MyBinding {
}
