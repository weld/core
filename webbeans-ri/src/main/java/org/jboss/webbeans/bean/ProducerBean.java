package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.HashSet;

import org.jboss.webbeans.ManagerImpl;

public abstract class ProducerBean<T, S> extends AbstractBean<T, S> {

   protected AbstractClassBean<?> declaringBean;

   public ProducerBean(ManagerImpl manager, AbstractClassBean<?> declaringBean) {
      super(manager);
      this.declaringBean = declaringBean;
   }

   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType() {
      return deploymentType = declaringBean.getDeploymentType();
   }

   /**
    * Initializes the API types
    */
   @Override
   protected void initApiTypes() {
      if (getType().isArray() || getType().isPrimitive())
      {
         apiTypes = new HashSet<Class<?>>();
         apiTypes.add(getType());
         apiTypes.add(Object.class);
      }
      else if (getType().isInterface())
      {
         super.initApiTypes();
         apiTypes.add(Object.class);
      }
      else
      {
         super.initApiTypes();
      }
   }

   /**
    * Initializes the type
    */
   @Override
   protected void initType()
   {
      try
      {
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e) 
      {
         throw new RuntimeException(" Cannot cast producer type " + getAnnotatedItem().getType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
   }
   
   /**
    * Returns the declaring bean
    * 
    * @return The bean representation
    */
   public AbstractClassBean<?> getDeclaringBean() {
      return declaringBean;
   }

}