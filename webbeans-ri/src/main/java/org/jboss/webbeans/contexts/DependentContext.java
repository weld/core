package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

import javax.webbeans.Dependent;

public class DependentContext extends PseudoContext
{

   public DependentContext(Class<? extends Annotation> scopeType)
   {
      super(Dependent.class);
   }

   @Override
   public String toString()
   {
      return "Dependent context";
   }

}
