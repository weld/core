package org.jboss.webbeans.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Destructor;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;

public class EnterpriseComponentModel<T> extends AbstractEnterpriseComponentModel<T>
{

   private EnterpriseConstructor<T> constructor;
   
   private String location;
   
   public EnterpriseComponentModel(AnnotatedType<T> annotatedItem,
         AnnotatedType<T> xmlAnnotatedItem, ManagerImpl container)
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
   
   public ComponentConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   @Override
   public String toString()
   {
      return "EnterpriseComponentModel[" + getType().getName() + "]";
   }

   @Override
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Enterprise Component; declaring class: " + getType() +";";
      }
      return location;
   }
   
// TODO loggigng
   protected void initRemoveMethod(ManagerImpl container)
   {
      if (getEjbMetaData().isStateful())
      {
         if (getEjbMetaData().getRemoveMethods().size() == 1)
         {
            super.removeMethod = new InjectableMethod<Object>(getEjbMetaData().getRemoveMethods().get(0));
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
               super.removeMethod = new InjectableMethod<Object>(possibleRemoveMethods.get(0)); 
            }
            else if (possibleRemoveMethods.size() > 1)
            {
               throw new RuntimeException("Multiple remove methods are annotated @Destroys for " + getType());
            }
            else if (possibleRemoveMethods.size() == 0)
            {
               throw new RuntimeException("Multiple remove methods are declared, and none are annotated @Destroys for " + getType());
            }
         }
         else if (getEjbMetaData().getRemoveMethods().size() == 0)
         {
            throw new RuntimeException("Stateful enterprise bean component has no remove methods declared for " + getType());
         }
      }
      else
      {
         List<Method> destroysMethods = Reflections.getMethods(getType(), Destructor.class);
         if (destroysMethods.size() > 0)
         {
            throw new RuntimeException("Only stateful enterprise bean components can have methods annotated @Destroys; " + getType() + " is not a stateful enterprise bean component");
         }
      }
   }

}
