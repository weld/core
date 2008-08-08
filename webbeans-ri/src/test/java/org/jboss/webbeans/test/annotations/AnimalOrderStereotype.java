package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.Stereotype;

import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Order;

@Stereotype(requiredTypes={Animal.class, Order.class})
@Target( { TYPE })
@Retention(RUNTIME)
public @interface AnimalOrderStereotype
{

}
