package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Produces;

public class Tampere
{
   @Produces
   @Dependent
   public Violation create(@Current Violation reference)
   {
      return reference;
   }
}
