package org.jboss.webbeans.test.components;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.RequestScoped;
import javax.webbeans.Stereotype;

@Stereotype(requiredTypes=Animal.class, supportedScopes=RequestScoped.class)
@Target( { TYPE })
@Retention(RUNTIME)
public @interface RequestScopedAnimalStereotype
{

}
