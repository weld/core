package org.jboss.webbeans.test.contexts.invalid;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.Dependent;
import javax.webbeans.InterceptorBindingType;

@InterceptorBindingType
@Dependent
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface CityBinding
{

}
