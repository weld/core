package org.jboss.webbeans.test.unit.definition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.stereotype.Stereotype;



@Stereotype
@Target( { TYPE })
@Retention(RUNTIME)
@interface AnimalOrderStereotype
{

}
