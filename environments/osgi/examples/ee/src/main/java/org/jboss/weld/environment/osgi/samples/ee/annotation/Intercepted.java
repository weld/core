package org.jboss.weld.environment.osgi.samples.ee.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.interceptor.InterceptorBinding;

/**
 * Stereotype for View bean. The view bean is responsible for
 * UI data.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
public @interface Intercepted {
}