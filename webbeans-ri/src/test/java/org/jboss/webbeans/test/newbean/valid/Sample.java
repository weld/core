package org.jboss.webbeans.test.newbean.valid;

import javax.webbeans.Initializer;
import javax.webbeans.New;
import javax.webbeans.Produces;


public class Sample
{
   @New
   WrappedBean reference;

   @Initializer
   public Sample(@New WrappedBean reference)
   {
   }

   @Initializer
   public void init(@New WrappedBean reference)
   {
   }

   @Produces
   Object produce(@New WrappedBean reference)
   {
      return new Object();
   }
}
