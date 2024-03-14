package org.jboss.weld.tests.proxy.ignoreinvalidmethods.inheritance;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Secure {

}
