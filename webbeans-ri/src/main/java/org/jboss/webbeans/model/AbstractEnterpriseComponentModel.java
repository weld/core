package org.jboss.webbeans.model;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.introspector.AnnotatedType;

public abstract class AbstractEnterpriseComponentModel<T> extends
      AbstractClassComponentModel<T>
{

   private EjbMetaData<T> ejbMetaData;

   public AbstractEnterpriseComponentModel(AnnotatedType<T> annotatedItem,
         AnnotatedType<T> xmlAnnotatedItem)
   {
      super(annotatedItem, xmlAnnotatedItem);
      
   }

   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      ejbMetaData = container.getEjbManager().getEjbMetaData(getType());
      checkEnterpriseScopeAllowed();
   }
   
   protected EjbMetaData<T> getEjbMetaData()
   {
      return ejbMetaData;
   }
   
   /**
    * Check that the scope type is allowed by the stereotypes on the component and the component type
    * @param type 
    */
   protected void checkEnterpriseScopeAllowed()
   {
      if (getEjbMetaData().isStateless() && !getScopeType().annotationType().equals(Dependent.class))
      {
         throw new RuntimeException("Scope " + getScopeType() + " is not allowed on stateless enterpise bean components for " + getType() + ". Only @Dependent is allowed on stateless enterprise bean components");
      }
      if (getEjbMetaData().isSingleton() && (!getScopeType().annotationType().equals(Dependent.class) || !getScopeType().annotationType().equals(ApplicationScoped.class)))
      {
         throw new RuntimeException("Scope " + getScopeType() + " is not allowed on singleton enterpise bean components for " + getType() + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise bean components");
      }
   }

}