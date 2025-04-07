package org.jboss.weld.tests.interceptors.multipleMethods;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface MyBinding {
}
