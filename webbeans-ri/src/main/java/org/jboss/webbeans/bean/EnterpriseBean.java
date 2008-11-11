package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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
import org.jboss.webbeans.introspector.impl.InjectableField;
import org.jboss.webbeans.introspector.impl.InjectableMethod;
import org.jboss.webbeans.introspector.impl.InjectableParameter;

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
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      if (removeMethod != null)
      {
         for (InjectableParameter<?> injectable : removeMethod.getParameters())
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
         throw new DefinitionException("Multiple @Destructor methods not allowed");
      }


      // <1 (0) @Destructors
      if (getEjbMetaData().getNoArgsRemoveMethod() != null)
      {
         if (getEjbMetaData().getRemoveMethods().size() > 1)
         {
            throw new DefinitionException("Multiple @Remove methods but no @Destructor found");
         }
         super.removeMethod = checkRemoveMethod(getEjbMetaData().getNoArgsRemoveMethod());
         return;
      }

      if (!getScopeType().equals(Dependent.class))
      {
         throw new DefinitionException("Only @Dependent scoped enterprise beans can be without remove methods");
      }
      
/*      
      if (getEjbMetaData().isStateful())
      {
         if (getEjbMetaData().getRemoveMethods().size() == 1)
         {
            super.removeMethod = checkRemoveMethod(getEjbMetaData().getRemoveMethods().iterator().next());
         }
         else if (getEjbMetaData().getRemoveMethods().size() > 1)
         {
            List<Method> possibleRemoveMethods = new ArrayList<Method>();
            for (Method removeMethod : getEjbMetaData().getRemoveMethods())
            {
               if (removeMethod.isAnnotationPresent(Destructor.class))
               {
                  possibleRemoveMethods.add(removeMethod);
               }
            }
            if (possibleRemoveMethods.size() == 1)
            {
               super.removeMethod = checkRemoveMethod(possibleRemoveMethods.iterator().next());
            }
            else if (possibleRemoveMethods.size() == 2)
            {
               Method firstMethod = possibleRemoveMethods.get(0);
               Method secondMethod = possibleRemoveMethods.get(1);
               // If the method has both the @Remove and @Destructor annotation, it gets added twice in 
               // the EjbMetaData constructor.
               if (!firstMethod.equals(secondMethod)) {
                  throw new DefinitionException("Multiple remove methods are annotated @Remove/@Destructor for " + getType());
               }
               super.removeMethod = checkRemoveMethod(possibleRemoveMethods.iterator().next());
            }
            else if (possibleRemoveMethods.size() > 2) 
            {
               throw new DefinitionException("Multiple remove methods are annotated @Remove/@Destructor for " + getType());
            }
            else if (possibleRemoveMethods.size() == 0)
            {
               throw new RuntimeException("Multiple remove methods are declared, and none are annotated @Destructor for " + getType());
            }
         }
         else if (getEjbMetaData().getRemoveMethods().isEmpty() && !getScopeType().equals(Dependent.class))
         {
            throw new DefinitionException("No remove methods declared for non-dependent scoped bean " + getType());
         }
      }
      else
      {
         List<Method> destroysMethods = Reflections.getMethods(getType(), Destructor.class);
         if (destroysMethods.size() > 0)
         {
            throw new DefinitionException("Only stateful enterprise beans can have methods annotated @Destructor; " + getType() + " is not a stateful enterprise bean");
         }
      }
      */
   }
   

   private InjectableMethod<?> checkRemoveMethod(Method method)
   {
      if (method.isAnnotationPresent(Destructor.class) && !method.isAnnotationPresent(EJB.REMOVE_ANNOTATION)) {
         throw new DefinitionException("Methods marked @Destructor must also be marked @Remove on " + method.getName());
      }
      if (method.isAnnotationPresent(Initializer.class)) {
         throw new DefinitionException("Remove methods cannot be initializers on " + method.getName());
      }
      if (method.isAnnotationPresent(Produces.class)) {
         throw new DefinitionException("Remove methods cannot be producers on " + method.getName());
      }
      if (hasParameterAnnotation(method.getParameterAnnotations(), Disposes.class)) {
         throw new DefinitionException("Remove method can't have @Disposes annotated parameters on " + method.getName());
      }
      if (hasParameterAnnotation(method.getParameterAnnotations(), Observes.class)) {
         throw new DefinitionException("Remove method can't have @Observes annotated parameters on " + method.getName());
      }
      return new InjectableMethod<Object>(method);
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
      for (InjectableMethod<Object> initializer : getInitializerMethods())
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
      for (InjectableField<?> injectableField : getInjectableFields())
      {
         injectableField.inject(instance, getManager());
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
   
   //FIXME move up?
   private boolean hasParameterAnnotation(Annotation[][] parameterAnnotations, Class<? extends Annotation> clazz)
   {
      for (Annotation[] parameter : parameterAnnotations) {
         for (Annotation annotation : parameter) {
            if (annotation.annotationType() == clazz) {
               return true;
            }
         }
      }
      return false;
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
   
   @Override
   public String toString()
   {
      return "EnterpriseBean[" + getType().getName() + "]";
   }

   
   
}
