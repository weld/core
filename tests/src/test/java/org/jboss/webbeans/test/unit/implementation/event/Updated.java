package org.jboss.webbeans.test.unit.implementation.event;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.BindingType;

@BindingType
@Retention(RUNTIME)
@Target( { TYPE, METHOD, FIELD, PARAMETER })
@Documented
@interface Updated
{

}
