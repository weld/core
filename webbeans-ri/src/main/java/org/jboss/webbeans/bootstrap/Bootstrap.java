package org.jboss.webbeans.bootstrap;

import static org.jboss.webbeans.util.BeanFactory.createEnterpriseBean;
import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.util.HashSet;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class Bootstrap
{
   
   private ManagerImpl manager;
   
   public Bootstrap()
   {
      this(new ManagerImpl());
   }
   
   protected Bootstrap(ManagerImpl manager)
   {
      this.manager = manager;
   }
   
   /**
    * Register any beans defined by the provided classes with the manager
    */
   public void registerBeans(Class<?>...classes)
   {
      Set<AbstractBean<?, ?>> beans = discoverBeans(classes);
      for (Bean<?> bean : beans)
      {
         manager.addBean(bean);
      }
   }
   
   /**
    * Discover any beans defined by the provided classes
    * 
    * Beans discovered are not registered with the manager
    */
   public Set<AbstractBean<?, ?>> discoverBeans(Class<?>... classes)
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      for (Class<?> clazz : classes)
      {
         AbstractClassBean<?> bean;
         if (manager.getModelManager().getEjbMetaData(clazz).isEjb())
         {
            bean = createEnterpriseBean(clazz, manager);
         }
         else
         {
            bean = createSimpleBean(clazz, manager);
         }
         beans.add(bean);
         for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
         {
            beans.add(createProducerMethodBean(producerMethod.getType(), producerMethod, manager, bean));
         }
         
      }
      return beans;
   }
   
}
