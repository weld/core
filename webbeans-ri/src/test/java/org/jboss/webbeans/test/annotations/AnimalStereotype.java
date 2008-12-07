package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.RequestScoped;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.test.beans.Animal;

@Stereotype(requiredTypes=Animal.class)
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@RequestScoped
public @interface AnimalStereotype
{

}
