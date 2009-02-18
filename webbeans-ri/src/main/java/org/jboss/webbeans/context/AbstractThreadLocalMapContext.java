package org.jboss.webbeans.context;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.context.beanmap.BeanMap;

public abstract class AbstractThreadLocalMapContext extends AbstractMapContext
{
   
   private final ThreadLocal<BeanMap> beanMap;

   public AbstractThreadLocalMapContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      this.beanMap = new ThreadLocal<BeanMap>();
   }

   /**
    * Gets the bean map
    * 
    * @returns The bean map
    * @see org.jboss.webbeans.context.AbstractContext#getNewEnterpriseBeanMap()
    */
   @Override
   public BeanMap getBeanMap()
   {
      return beanMap.get();
   }

   /**
    * Sets the bean map
    * 
    * @param beanMap The bean map
    */
   public void setBeanMap(BeanMap beanMap)
   {
      this.beanMap.set(beanMap);
   }
   
}