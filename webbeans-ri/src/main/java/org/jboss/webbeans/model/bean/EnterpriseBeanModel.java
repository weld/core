package org.jboss.webbeans.model.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.Produces;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.util.Reflections;

public class EnterpriseBeanModel<T> extends AbstractEnterpriseBeanModel<T>
{

   private EnterpriseConstructor<T> constructor;
   
   private String location;
   
   public EnterpriseBeanModel(AnnotatedClass<T> annotatedItem,
         AnnotatedClass<T> xmlAnnotatedItem, ManagerImpl container)
   {
      super(annotatedItem, xmlAnnotatedItem);
      init(container);
   }
   
   @Override
   protected void init(ManagerImpl container)
   {
      super.init(container);
      this.constructor = new EnterpriseConstructor<T>(getEjbMetaData());
      initRemoveMethod(container);
      initInjectionPoints();
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
   
   public EnterpriseConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   @Override
   public String toString()
   {
      return "EnterpriseBean[" + getType().getName() + "]";
   }

   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Enterprise Bean; declaring class: " + getType() +";";
      }
      return location;
   }
   
   
// TODO logging
   protected void initRemoveMethod(ManagerImpl container)
   {
      if (getEjbMetaData().isStateful())
      {
         if (getEjbMetaData().getRemoveMethods().size() == 1)
         {
//            super.removeMethod = new InjectableMethod<Object>(getEjbMetaData().getRemoveMethods().get(0));
            super.removeMethod = checkRemoveMethod(getEjbMetaData().getRemoveMethods().get(0));
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
               super.removeMethod = checkRemoveMethod(possibleRemoveMethods.get(0));
            }
            else if (possibleRemoveMethods.size() > 1)
            {
               throw new DefinitionException("Multiple remove methods are annotated @Destructor for " + getType());
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
   protected AbstractClassBeanModel<? extends T> getSpecializedType()
   {
      //TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if ( superclass!=null )
      {
         return new EnterpriseBeanModel(new SimpleAnnotatedClass(superclass), null, manager);
      }
      else {
         throw new RuntimeException();
      }
      
   }

}
