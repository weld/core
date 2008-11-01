package org.jboss.webbeans.model.bean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Destructor;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.BeanConstructor;
import org.jboss.webbeans.injectable.EnterpriseConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.test.util.Util;
import org.jboss.webbeans.util.Reflections;

public class EnterpriseBeanModel<T> extends AbstractEnterpriseBeanModel<T>
{

   private EnterpriseConstructor<T> constructor;
   
   private String location;
   
   public EnterpriseBeanModel(AnnotatedType<T> annotatedItem,
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
   
   public BeanConstructor<T> getConstructor()
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
            throw new RuntimeException("Stateful enterprise bean bean has no remove methods declared for " + getType());
         }
      }
      else
      {
         List<Method> destroysMethods = Reflections.getMethods(getType(), Destructor.class);
         if (destroysMethods.size() > 0)
         {
            throw new RuntimeException("Only stateful enterprise beans can have methods annotated @Destroys; " + getType() + " is not a stateful enterprise bean");
         }
      }
   }
   
   @Override
   protected AbstractClassBeanModel<? extends T> getSpecializedType()
   {
      //TODO: lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if ( superclass!=null )
      {
         return new EnterpriseBeanModel( new SimpleAnnotatedType( superclass ), Util.getEmptyAnnotatedType( getAnnotatedItem().getType().getSuperclass() ), container );
      }
      else {
         throw new RuntimeException();
      }
      
   }

}
