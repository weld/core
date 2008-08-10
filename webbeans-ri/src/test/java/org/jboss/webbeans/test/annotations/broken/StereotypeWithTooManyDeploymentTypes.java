package org.jboss.webbeans.test.annotations.broken;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.Production;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;

@Stereotype
@Target( { TYPE })
@Retention(RUNTIME)
@Production
@AnotherDeploymentType
public @interface StereotypeWithTooManyDeploymentTypes
{

}
