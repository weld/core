package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.Stereotype;

import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.Order;

@Stereotype(requiredTypes={Animal.class, Order.class})
@Target( { TYPE })
@Retention(RUNTIME)
public @interface AnimalOrderStereotype
{

}
