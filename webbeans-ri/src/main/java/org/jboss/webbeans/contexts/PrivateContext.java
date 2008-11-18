package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrivateContext extends AbstractContext
{
   private ThreadLocal<AtomicBoolean> active;
   protected ThreadLocal<BeanMap> beans;

   public PrivateContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new ThreadLocal<BeanMap>();
      beans.set(new SimpleBeanMap());
      active = new ThreadLocal<AtomicBoolean>();
      active.set(new AtomicBoolean(true));
   }

   @Override
   protected AtomicBoolean getActive()
   {
      return active.get();
   }

   @Override
   protected BeanMap getBeanMap()
   {
      return beans.get();
   }

}
