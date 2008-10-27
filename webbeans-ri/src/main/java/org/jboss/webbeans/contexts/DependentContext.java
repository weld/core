package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

public class DependentContext extends AbstractContext
{

   public DependentContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
   }

}
