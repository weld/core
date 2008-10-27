package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

public abstract class PseudoContext extends AbstractContext
{

   public PseudoContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
   }

}
