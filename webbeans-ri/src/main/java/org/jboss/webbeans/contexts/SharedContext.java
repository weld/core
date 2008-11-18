package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The abstraction of a shared context with common beans but 
 * with a private active state
 *  
 * @author Nicklas Karlsson
 */
public class SharedContext extends AbstractContext
{
   private BeanMap beans;
   private ThreadLocal<AtomicBoolean> active;

   public SharedContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new SimpleBeanMap();
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
      return beans;
   }

}