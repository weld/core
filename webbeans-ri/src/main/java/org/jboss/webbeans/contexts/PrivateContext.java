package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrivateContext extends AbstractContext
{
   private ThreadLocal<AtomicBoolean> active;
   private ThreadLocal<BeanMap> beans;

   public PrivateContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new ThreadLocal<BeanMap>();
      beans.set(new BeanMap());
      active = new ThreadLocal<AtomicBoolean>();
      active.set(new AtomicBoolean(true));
   }

   public void setBeans(BeanMap beans)
   {
      this.beans.set(beans);
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
