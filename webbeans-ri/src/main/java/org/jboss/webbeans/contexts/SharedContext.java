package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

public class SharedContext extends AbstractContext
{
   private BeanMap beans;
   private AtomicBoolean active;

   public SharedContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new BeanMap();
      active = new AtomicBoolean(true);
   }

   @Override
   protected AtomicBoolean getActive()
   {
      return active;
   }

   @Override
   protected BeanMap getBeanMap()
   {
      return beans;
   }

}