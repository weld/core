package org.jboss.weld.tests.classDefining.packPrivate.interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.interceptor.InterceptorBinding;

@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface SomeBinding {
}
