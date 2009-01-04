package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Decorates;
import javax.webbeans.Decorator;
import javax.webbeans.Dependent;

@Decorator
@Dependent
public class CityDecorator implements CityInterface
{
   @Decorates
   private Raisio decorates;
   
   public void foo()
   {
   }

}
