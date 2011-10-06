package org.jboss.weld.environment.osgi.samples.ee.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;

/**
 * Stereotype for Presenter bean. The presenter bean is responsible for
 * UI logic.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 */
@Named
@RequestScoped
@Stereotype
@Intercepted
@Retention(RUNTIME)
@Target({METHOD, FIELD, TYPE})
public @interface Presenter {
}