package org.jboss.webbeans.tck;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;

import org.jboss.jsr299.tck.spi.Beans;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.AbstractContext;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.util.Proxies;

/**
 * Implements the Beans SPI for the TCK specifically for the JBoss RI.
 * 
 * @author Shane Bryzak
 * @author Pete Muir
 * @author David Allen
 * 
 */
public class BeansImpl implements Beans
{

   public boolean isEnterpriseBean(Class<?> clazz)
   {
      return CurrentManager.rootManager().getNewEnterpriseBeanMap().containsKey(clazz);
   }

   public boolean isEntityBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getNewEnterpriseBeanMap().containsKey(clazz))
      {
         EjbDescriptor<?> ejbDescriptor = CurrentManager.rootManager().getNewEnterpriseBeanMap().get(clazz).getEjbDescriptor();
         if (!ejbDescriptor.isMessageDriven() && !ejbDescriptor.isSingleton() && !ejbDescriptor.isStateful() && !ejbDescriptor.isStateless())
         {
            return true;
         }
      }
      return false;
   }

   public boolean isStatefulBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getNewEnterpriseBeanMap().containsKey(clazz))
      {
         EjbDescriptor<?> ejbDescriptor = CurrentManager.rootManager().getNewEnterpriseBeanMap().get(clazz).getEjbDescriptor();
         if (ejbDescriptor.isStateful())
         {
            return true;
         }
      }
      return false;
   }

   public boolean isStatelessBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getNewEnterpriseBeanMap().containsKey(clazz))
      {
         EjbDescriptor<?> ejbDescriptor = CurrentManager.rootManager().getNewEnterpriseBeanMap().get(clazz).getEjbDescriptor();
         if (ejbDescriptor.isStateless())
         {
            return true;
         }
      }
      return false;
   }
   
   public boolean isProxy(Object instance)
   {
      return Proxies.isProxy(instance);
   }

   public <T> T getEnterpriseBean(Class<? extends T> beanType, Class<T> localInterface)
   {
      // Get the EJB Descriptor and resolve it
      if (CurrentManager.rootManager().getNewEnterpriseBeanMap().containsKey(beanType))
      {  
         EjbDescriptor<?> ejbDescriptor = CurrentManager.rootManager().getNewEnterpriseBeanMap().get(beanType).getEjbDescriptor().delegate();
         return CurrentManager.rootManager().getServices().get(EjbServices.class).resolveEjb(ejbDescriptor).getBusinessObject(localInterface);
      }   
      throw new NullPointerException("No EJB found for " + localInterface.getName() + " on bean " + beanType.getName());
   }

   public <T> T createBeanInstance(Bean<T> bean)
   {
      return (T) CurrentManager.rootManager().getCurrent().getReference(bean, Object.class, CurrentManager.rootManager().getCurrent().createCreationalContext(bean));
   }

   public <T> void destroyAndRemoveBeanInstance(Bean<T> bean, T instance)
   {
      Context context = CurrentManager.rootManager().getCurrent().getContext(bean.getScopeType());
      if (context instanceof AbstractContext)
      {
         ((AbstractContext) context).destroyAndRemove(bean, instance);
      }
      else
      {
         throw new IllegalStateException("Don't know how to destroy a bean from " + context);
      }
   }



}
