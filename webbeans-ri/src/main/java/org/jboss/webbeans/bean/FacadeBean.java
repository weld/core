package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

public abstract class FacadeBean<T, S, P> extends AbstractBean<T, S> {

   protected AnnotatedItem<T, S> annotatedItem;

   public FacadeBean(AnnotatedItem<T, S> field, ManagerImpl manager) {
      super(manager);
      this.annotatedItem = field;
      init();
   }

   /**
    * Initializes the bean
    * 
    * Calls super method and validates the annotated item
    */
   protected void init() {
      super.init();
      checkAnnotatedItem();
   }

   /**
    * Validates the annotated item
    */
   private void checkAnnotatedItem() {
      Type[] actualTypeArguments = annotatedItem.getActualTypeArguments();
      if (actualTypeArguments.length != 1)
      {
         throw new DefinitionException("Event must have type arguments");
      }
      if (!(actualTypeArguments[0] instanceof Class))
      {
         throw new DefinitionException("Event must have concrete type argument");
      }
   }

   protected Annotation[] getBindingTypesArray() {
      return annotatedItem.getBindingTypesAsArray();
   }

   protected Class<P> getTypeParameter() {
      return (Class<P>) annotatedItem.getType().getTypeParameters()[0].getClass();
   }

   @Override
   protected void initScopeType() {
      this.scopeType = Dependent.class;
   }

   @Override
   protected void initDeploymentType() {
      this.deploymentType = Standard.class;
   }

   @Override
   protected AnnotatedItem<T, S> getAnnotatedItem() {
      return annotatedItem;
   }

   @Override
   protected String getDefaultName() {
      return null;
   }

   @Override
   protected void initType() {
      try
      {
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e)
      {
         // TODO: Expand error
         throw new IllegalArgumentException("Type mismatch");
      }
   }

   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType() {
      return Production.class;
   }

}