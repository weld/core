package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;

import org.jboss.weld.bean.SessionBean;

public class ProcessSessionBeanImpl<X> extends AbstractProcessBean<Object, SessionBean<Object>> implements ProcessSessionBean<X>
{

   public ProcessSessionBeanImpl(SessionBean<Object> bean)
   {
      super(bean);
   }

   public AnnotatedType<X> getAnnotatedSessionBeanClass()
   {
      return (AnnotatedType<X>) getBean().getAnnotatedItem();
   }

   public String getEjbName()
   {
      return getBean().getEjbDescriptor().getEjbName();
   }

   public SessionBeanType getSessionBeanType()
   {
      if (getBean().getEjbDescriptor().isStateless())
      {
         return SessionBeanType.STATELESS;
      }
      else if (getBean().getEjbDescriptor().isStateful())
      {
         return SessionBeanType.STATEFUL;
      }
      else if (getBean().getEjbDescriptor().isSingleton())
      {
         return SessionBeanType.SINGLETON;
      }
      else
      {
         throw new IllegalStateException("Bean type is not STATELESS, STATEFUL or SINGELTON " + getBean());
      }
   }

   public AnnotatedType<Object> getAnnotatedBeanClass()
   {
      return getBean().getAnnotatedItem();
   }

}
