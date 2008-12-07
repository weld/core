package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.IllegalProductException;

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

   /**
    * Validates the producer method
    */
   protected void checkProducerReturnType() {
      for (Type type : getAnnotatedItem().getActualTypeArguments())
      {
         if (!(type instanceof Class))
         {
            throw new DefinitionException("Producer type cannot be parameterized with type parameter or wildcard");
         }
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init() {
      super.init();
      checkProducerReturnType();
   }

   protected void checkReturnValue(T instance) {
      if (instance == null && !getScopeType().equals(Dependent.class))
      {
         throw new IllegalProductException("Cannot return null from a non-dependent producer method");
      }
   }

   protected Object getReceiver() {
      return getAnnotatedItem().isStatic() ? 
              null : manager.getInstance(getDeclaringBean());
   }

}