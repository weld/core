package org.jboss.webbeans.bean;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Decorator;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Interceptor;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.Specializes;
import javax.webbeans.manager.EnterpriseBeanLookup;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public class EnterpriseBean<T> extends AbstractClassBean<T>
{
   
   private String location;
   
   private EjbMetaData<T> ejbMetaData;
   
   public EnterpriseBean(Class<T> type, ManagerImpl container)
   {
      super(type, container);
      init();
   }
   
   @Override
   protected void init()
   {
      super.init();
      ejbMetaData = new EjbMetaData<T>(getType());
      initRemoveMethod();
      initInjectionPoints();
      checkEnterpriseBeanTypeAllowed();
      checkEnterpriseScopeAllowed();
      checkConflictingRoles();
      checkSpecialization();
      checkRemoveMethod();
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }
   
   protected void checkConflictingRoles()
   {
      if (getType().isAnnotationPresent(Interceptor.class))
      {
         throw new DefinitionException("Enterprise beans cannot be interceptors");
      }
      if (getType().isAnnotationPresent(Decorator.class))
      {
         throw new DefinitionException("Enterprise beans cannot be decorators");
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
   
   private void checkSpecialization()
   {
      if (!getType().isAnnotationPresent(Specializes.class))
      {
         return;
      }
      if (!isDefinedInXml())
      {
         if (!getManager().getMetaDataCache().getEjbMetaData(getAnnotatedItem().getSuperclass().getType()).isEjb())
         {
            throw new DefinitionException("Annotation defined specializing EJB must have EJB superclass");
         }
      } 
      else
      {
         if (getManager().getMetaDataCache().getEjbMetaData(getAnnotatedItem().getSuperclass().getType()).isEjb())
         {
            throw new DefinitionException("XML defined specializing EJB must have annotation defined EJB implementation");
         }
      }
   }
   
// TODO logging
   protected void initRemoveMethod()
   {
      if (!getEjbMetaData().isStateful())
      {
         // Nothing to do for stateless enterprise beans;
         return;
      }
      
      // >1 @Destructor
      if (getEjbMetaData().getDestructorMethods().size() > 1)
      {
         throw new DefinitionException("Multiple @Destructor methods not allowed on " + getAnnotatedItem());
      }


      // <1 (0) @Destructors
      if (getEjbMetaData().getNoArgsRemoveMethods().size() == 1)
      {
         super.removeMethod = getEjbMetaData().getNoArgsRemoveMethods().get(0);
         return;
      }

      if (!getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Only @Dependent scoped enterprise beans can be without remove methods");
      }

   }
   
   private void checkRemoveMethod()
   {
      if (removeMethod != null)
      {
         if (removeMethod.isAnnotationPresent(Destructor.class) && !removeMethod.isAnnotationPresent(EJB.REMOVE_ANNOTATION)) 
         {
            throw new DefinitionException("Methods marked @Destructor must also be marked @Remove on " + removeMethod.getName());
         }
         else if (removeMethod.isAnnotationPresent(Initializer.class)) 
         {
            throw new DefinitionException("Remove methods cannot be initializers on " + removeMethod.getName());
         }
         else if (removeMethod.isAnnotationPresent(Produces.class)) 
         {
            throw new DefinitionException("Remove methods cannot be producers on " + removeMethod.getName());
         }
         else if (removeMethod.getAnnotatedParameters(Disposes.class).size() > 0) 
         {
            throw new DefinitionException("Remove method can't have @Disposes annotated parameters on " + removeMethod.getName());
         }
         else if (removeMethod.getAnnotatedParameters(Observes.class).size() > 0) 
         {
            throw new DefinitionException("Remove method can't have @Observes annotated parameters on " + removeMethod.getName());
         }
      }
   }

   @Override
   public T create()
   {
      T instance = (T) getManager().getInstanceByType(EnterpriseBeanLookup.class).lookup(ejbMetaData.getEjbName());
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      callInitializers(instance);
      return instance;      
   }
   
   @Override
   public void destroy(T instance)
   {
      super.destroy(instance);
   }

   protected void callInitializers(T instance)
   {
      for (AnnotatedMethod<Object> initializer : getInitializerMethods())
      {
         initializer.invoke(getManager(), instance);
      }
   }
   
   protected void injectEjbAndCommonFields()
   {
      // TODO
   }
   
   protected void injectBoundFields(T instance)
   {
      for (AnnotatedField<?> field : getInjectableFields())
      {
         field.inject(instance, getManager());
      }
   }

   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Enterprise Bean; declaring class: " + getType() +";";
      }
      return location;
   }

   @Override
   protected AbstractBean<? extends T, Class<T>> getSpecializedType()
   {
      //TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if ( superclass!=null )
      {
         return new EnterpriseBean(superclass, getManager());
      }
      else {
         throw new RuntimeException();
      }
      
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
   
   @Override
   public String toString()
   {
      return "EnterpriseBean[" + getType().getName() + "]";
   }

   
   
}
