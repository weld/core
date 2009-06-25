package org.jboss.webbeans.test.unit.definition;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.stereotype.Stereotype;

@Stereotype(requiredTypes=Animal.class, supportedScopes=RequestScoped.class)
@Target( { TYPE })
@Retention(RUNTIME)
public @interface RequestScopedAnimalStereotype
{

}
