package org.jboss.weld.tests.contexts.creational;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.interceptor.InterceptorBinding;

@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface AroundInvokeBinding {
}
