package org.jboss.webbeans.test.newbean.valid;

import javax.webbeans.New;
import javax.webbeans.Produces;


public class AnnotatedProducerParameter
{
   @Produces
   Object produce(@New WrappedBean reference)
   {
      return new Object();
   }
}
