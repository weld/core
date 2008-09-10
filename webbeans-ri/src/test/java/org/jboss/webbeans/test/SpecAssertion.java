package org.jboss.webbeans.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface SpecAssertion
{
   
   public String section();
   
   public String note() default "";

}
