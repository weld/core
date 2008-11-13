package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.IllegalProductException;
import javax.webbeans.Observes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;

public class ProducerMethodBean<T> extends AbstractBean<T, Method>
{
   
   private AnnotatedMethod<T> method;
   private AbstractClassBean<?> declaringBean;
   
   // Cached values
   private String location;

   public ProducerMethodBean(Method method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      this(new AnnotatedMethodImpl<T>(method, declaringBean.getAnnotatedItem()), declaringBean, manager);
   }
   
   public ProducerMethodBean(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      super(manager);
      this.method = method;
      this.declaringBean = declaringBean;
      init();
   }

   @Override
   public T create()
   {
      T instance = method.invoke(getManager(), getManager().getInstance(getDeclaringBean()));
      if (instance == null && !getScopeType().equals(Dependent.class))
      {
         throw new IllegalProductException("Cannot return null from a non-dependent method");
      }
      return instance;
   }

   @Override
   protected void init()
   {
      super.init();
      checkProducerMethod();
      initRemoveMethod();
      initInjectionPoints();
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (AnnotatedParameter<Object> parameter : method.getParameters())
      {
         injectionPoints.add(parameter);
      }
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }
   
   @Override
   protected void initDeploymentType()
   {
      super.initDeploymentType();
      if (getDeploymentType() == null)
      {
         deploymentType = declaringBean.getDeploymentType();
      }
   }
   
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().isStatic())
      {
         throw new DefinitionException("Producer method cannot be static " + method);
      }
      else if (getAnnotatedItem().isAnnotationPresent(Destructor.class))
      {
         throw new DefinitionException("Producer method cannot be annotated @Destructor");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Observes");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Disposes");
      }
      else if (getAnnotatedItem().getActualTypeArguments().length > 0)
      {
         for (Type type : getAnnotatedItem().getActualTypeArguments())
         {
            if (!(type instanceof Class))
            {
               throw new DefinitionException("Producer method cannot return type parameterized with type parameter or wildcard");
            }
         }
      }
   }
   
   protected void initRemoveMethod()
   {
      Set<AnnotatedMethod<Object>> disposalMethods = getManager().resolveDisposalMethods(getType(), getBindingTypes().toArray(new Annotation[0]));
      if (disposalMethods.size() == 1)
      {
         removeMethod = disposalMethods.iterator().next();
      }
      else if (disposalMethods.size() > 1)
      {
         // TODO List out found disposal methods
         throw new RuntimeException(getLocation() + "Cannot declare multiple disposal methods for this producer method");
      }
   }
   
   @Override
   public String toString()
   {
      return "ProducerMethodBean[" + getType().getName() + "]";
   }

   @Override
   protected AnnotatedMethod<T> getAnnotatedItem()
   {
      return method;
   }

   @Override
   protected String getDefaultName()
   {
      return method.getPropertyName();
   }

   @Override
   protected void initType()
   {
      try
      {
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e) 
      {
         throw new RuntimeException(getLocation() + " Cannot cast producer method return type " + method.getType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
   }
   
   @Override
   protected void initApiTypes()
   {
      if (getType().isArray() || getType().isPrimitive())
      {
         super.apiTypes = new HashSet<Class<?>>();
         super.apiTypes.add(getType());
         super.apiTypes.add(Object.class);
      }
      else if (getType().isInterface())
      {
         super.initApiTypes();
         super.apiTypes.add(Object.class);
      }
      else
      {
         super.initApiTypes();
      }
   }
   

   
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Producer Method; declaring class: " + method.getDeclaringClass() +"; producer method: " + method.toString() + ";";
      }
      return location;
   }
   
   public AnnotatedMethod<?> getDisposalMethod()
   {
      return removeMethod;
   }
   
   public AbstractClassBean<?> getDeclaringBean()
   {
      return declaringBean;
   }

   
}
