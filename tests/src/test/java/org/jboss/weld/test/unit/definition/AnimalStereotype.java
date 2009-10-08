package org.jboss.weld.test.unit.definition;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.stereotype.Stereotype;

@Stereotype
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@RequestScoped
@interface AnimalStereotype
{

}
