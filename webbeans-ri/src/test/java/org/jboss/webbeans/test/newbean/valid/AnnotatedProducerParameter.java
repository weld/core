package org.jboss.webbeans.test.newbean.valid;

import javax.webbeans.New;
import javax.webbeans.Produces;


public class AnnotatedProducerParameter
{
   @Produces
   Object produce(@New WrappedSimpleBean reference)
   {
      return new Object();
   }
}
