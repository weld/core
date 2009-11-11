package org.jboss.weld.bootstrap.events;

import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_TYPE_NOT_EJB;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.ForbiddenStateException;
import org.jboss.weld.bean.SessionBean;

public class ProcessSessionBeanImpl<X> extends AbstractProcessClassBean<Object, SessionBean<Object>> implements ProcessSessionBean<X>
{
   
   public static <X> void fire(BeanManagerImpl beanManager, SessionBean<Object> bean)
   {
      new ProcessSessionBeanImpl<X>(beanManager, bean) {}.fire();
   }

   public ProcessSessionBeanImpl(BeanManagerImpl beanManager, SessionBean<Object> bean)
   {
      super(beanManager, ProcessSessionBean.class, new Type[] { bean.getAnnotatedItem().getBaseType() }, bean);
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
         throw new ForbiddenStateException(BEAN_TYPE_NOT_EJB, getBean());
      }
   }

   public AnnotatedType<Object> getAnnotatedBeanClass()
   {
      return getBean().getAnnotatedItem();
   }

}
