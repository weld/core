package org.jboss.webbeans.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.Dependent;
import javax.webbeans.Destroys;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.ejb.EjbMetaData;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;

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
   protected void init(ContainerImpl container)
   {
      super.init(container);
      ejbMetaData = container.getEjbManager().getEjbMetaData(getType());
      initRemoveMethod(container);
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
   
   // TODO loggigng
   protected void initRemoveMethod(ContainerImpl container)
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
               if (removeMethod.isAnnotationPresent(Destroys.class))
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
         List<Method> destroysMethods = Reflections.getMethods(getType(), Destroys.class);
         if (destroysMethods.size() > 0)
         {
            throw new RuntimeException("Only stateful enterprise bean components can have methods annotated @Destroys; " + getType() + " is not a stateful enterprise bean component");
         }
      }
   }

}