package org.jboss.weld.osgi.examples.userdoc.helloworld.provider.api;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@InterceptorBinding
@Target({ TYPE, METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Presentation {
}
