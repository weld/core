package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.util.AnnotatedItem;

public class EnterpriseComponentModel<T> extends AbstractComponentModel<T>
{

   private SimpleConstructor<T> constructor;
   private InjectableMethod<?> removeMethod;  
   
   public EnterpriseComponentModel(AnnotatedItem annotatedItem,
         AnnotatedItem xmlAnnotatedItem, ContainerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem, container);
      this.constructor = initConstructor(getType());
      EjbMetaData<T> ejbMetaData = EJB.getEjbMetaData(getType());
      EnterpriseComponentModel.checkScopeAllowed(getMergedStereotypes(), getScopeType(), getType(), ejbMetaData);
      this.removeMethod = initRemoveMethod(ejbMetaData, getType());
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
   
   public SimpleConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   public InjectableMethod<?> getRemoveMethod()
   {
      return removeMethod;
   }

}
