package org.jboss.webbeans.test.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(value=TYPE)
@Retention(value=RUNTIME)
public @interface Singleton
{
   // EJB 3.1. Fake it 'til you make it.
}
