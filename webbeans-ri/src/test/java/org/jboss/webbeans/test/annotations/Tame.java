package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.BindingType;

@Target( { TYPE, METHOD, PARAMETER })
@Retention(RUNTIME)
@Documented
@BindingType
public @interface Tame
{

}
