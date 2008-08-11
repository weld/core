package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;

public class EnterpriseComponentModel<T> extends AbstractClassComponentModel<T>
{

   private EnterpriseConstructor<T> constructor;
   private InjectableMethod<?> removeMethod;  
   
   public EnterpriseComponentModel(AnnotatedType annotatedItem,
         AnnotatedType xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      EjbMetaData<T> ejbMetaData = container.getEjbManager().getEjbMetaData(getType());
      this.constructor = initConstructor(ejbMetaData, container);
      EnterpriseComponentModel.checkScopeAllowed(getMergedStereotypes(), getScopeType(), getType(), ejbMetaData);
      this.removeMethod = initRemoveMethod(ejbMetaData, getType());
   }
   
   protected static <T> EnterpriseConstructor<T> initConstructor(EjbMetaData<T> ejbMetaData, ContainerImpl container)
   {
      return new EnterpriseConstructor<T>(ejbMetaData);
   }

   /**
    * Check that the scope type is allowed by the stereotypes on the component and the component type
    * @param type 
    */
   protected static <T> void checkScopeAllowed(MergedStereotypesModel stereotypes, Annotation scopeType, Class<?> type, EjbMetaData<T> ejbMetaData)
   {
      if (ejbMetaData.isStateless() && !scopeType.annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed on stateless enterpise bean components for " + type + ". Only @Dependent is allowed on stateless enterprise bean components");
      }
      if (ejbMetaData.isSingleton() && (!scopeType.annotationType().equals(Dependent.class) || !scopeType.annotationType().equals(ApplicationScoped.class)))
      {
         throw new RuntimeException("Scope " + scopeType + " is not allowed on singleton enterpise bean components for " + type + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise bean components");
      }
   }
   
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

}
