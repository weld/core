package org.jboss.webbeans.test.newbean.valid;

import javax.webbeans.Initializer;
import javax.webbeans.New;


public class AnnotatedInitializerParameter
{
   @Initializer
   public void init(@New WrappedSimpleBean reference)
   {
   }
}
