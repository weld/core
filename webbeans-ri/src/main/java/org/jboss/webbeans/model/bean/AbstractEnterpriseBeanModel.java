package org.jboss.webbeans.model.bean;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Decorator;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Interceptor;
import javax.webbeans.Specializes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.introspector.AnnotatedClass;

public abstract class AbstractEnterpriseBeanModel<T> extends
      AbstractClassBeanModel<T>
{

   private EjbMetaData<T> ejbMetaData;

   public AbstractEnterpriseBeanModel(AnnotatedClass<T> annotatedItem,
         AnnotatedClass<T> xmlAnnotatedItem)
   {
      super(annotatedItem, xmlAnnotatedItem);
   }

   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      ejbMetaData = new EjbMetaData<T>(getType());
      checkEnterpriseBeanTypeAllowed();
      checkEnterpriseScopeAllowed();
      checkConflictingRoles();
      checkSpecialization();
   }

   private void checkSpecialization()
   {
      if (!getType().isAnnotationPresent(Specializes.class))
      {
         return;
      }
      if (annotationDefined)
      {
         if (!isEJB(getType().getSuperclass()))
         {
            throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
         }
      } else
      {
         if (!isEJB(getType()))
         {
            throw new DefinitionException("XML defined specializing EJB must have annotation defined EJB implementation");
         }
      }
   }

   private boolean isEJB(Class<? super T> clazz)
   {
      return clazz.isAnnotationPresent(EJB.SINGLETON_ANNOTATION)
            || clazz.isAnnotationPresent(EJB.STATEFUL_ANNOTATION)
            || clazz.isAnnotationPresent(EJB.STATELESS_ANNOTATION);
   }

   private void checkEnterpriseBeanTypeAllowed()
   {
      if (getEjbMetaData().isMessageDriven())
      {
         throw new DefinitionException(
               "Message Driven Beans can't be Web Beans");
      }
   }

   protected EjbMetaData<T> getEjbMetaData()
   {
      return ejbMetaData;
   }

   protected void checkConflictingRoles()
   {
      if (getType().isAnnotationPresent(Interceptor.class))
      {
         throw new DefinitionException("Enterprise beans can't be interceptors");
      }
      if (getType().isAnnotationPresent(Decorator.class))
      {
         throw new DefinitionException("Enterprise beans can't be decorators");
      }
   }

   /**
    * Check that the scope type is allowed by the stereotypes on the bean and
    * the bean type
    * 
    * @param type
    */
   protected void checkEnterpriseScopeAllowed()
   {
      if (getEjbMetaData().isStateless()
            && !getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Scope " + getScopeType()
               + " is not allowed on stateless enterpise beans for "
               + getType()
               + ". Only @Dependent is allowed on stateless enterprise beans");
      }
      if (getEjbMetaData().isSingleton()
            && (!(getScopeType().equals(Dependent.class) || getScopeType()
                  .equals(ApplicationScoped.class))))
      {
         throw new DefinitionException(
               "Scope "
                     + getScopeType()
                     + " is not allowed on singleton enterpise beans for "
                     + getType()
                     + ". Only @Dependent or @ApplicationScoped is allowed on singleton enterprise beans");
      }
   }

}