package org.jboss.webbeans.test.newbean.valid;

import javax.webbeans.Initializer;
import javax.webbeans.New;


public class AnnotatedConstructorParameter
{
   @Initializer
   public AnnotatedConstructorParameter(@New WrappedSimpleBean reference)
   {
   }
}
