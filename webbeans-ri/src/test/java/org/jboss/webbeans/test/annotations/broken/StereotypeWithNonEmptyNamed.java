package org.jboss.webbeans.test.annotations.broken;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.Named;
import javax.webbeans.Stereotype;

@Stereotype
@Target( { TYPE })
@Retention(RUNTIME)
@Named("foo")
public @interface StereotypeWithNonEmptyNamed
{

}
