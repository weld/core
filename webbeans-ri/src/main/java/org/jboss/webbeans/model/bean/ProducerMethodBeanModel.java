package org.jboss.webbeans.model.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;

import javax.webbeans.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.BeanConstructor;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.MethodConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;
import org.jboss.webbeans.util.Reflections;

public class ProducerMethodBeanModel<T> extends AbstractProducerBeanModel<T>
{
   
   private MethodConstructor<T> constructor;
   
   private AnnotatedItem<T, Method> xmlAnnotatedItem = new SimpleAnnotatedItem<T, Method>(new HashMap<Class<? extends Annotation>, Annotation>());
   private AnnotatedMethod<T> annotatedMethod;
   
   private BeanModel<?, ?> declaringBean;
   
   // Cached values
   private String location;
   private Type declaredBeanType;
   
   public ProducerMethodBeanModel(AnnotatedMethod<T> annotatedMethod, ManagerImpl container)
   {
      this.annotatedMethod = annotatedMethod;
      init(container);
   }
   
   @Override
   protected void init(ManagerImpl manager)
   {
      super.init(manager);
      checkProducerMethod();
      this.constructor = new MethodConstructor<T>(getAnnotatedItem().getDelegate());
      initRemoveMethod(manager);
      initInjectionPoints();
   }
   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (InjectableParameter<?> injectable : constructor.getParameters())
      {
         injectionPoints.add(injectable);
      }
      if (removeMethod != null)
      {
         for (InjectableParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }
   
   @Override
   protected void initDeploymentType(ManagerImpl container)
   {
      super.initDeploymentType(container);
      if (getDeploymentType() == null)
      {
         if (getDeclaringBean() == null)
         {
            initDeclaringBean(container);
         }
         deploymentType = declaringBean.getDeploymentType();
      }
   }

   protected void initDeclaringBean(ManagerImpl container)
   {
      declaringBean = container.getModelManager().getBeanModel(getAnnotatedItem().getDelegate().getDeclaringClass());
   }
   
   public BeanConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   protected void checkProducerMethod()
   {
      if (Modifier.isStatic(getAnnotatedItem().getDelegate().getModifiers()))
      {
         throw new RuntimeException("Producer method cannot be static " + annotatedMethod);
      }
      // TODO Check if declaring class is a WB bean
      if (!getScopeType().equals(Dependent.class) && Modifier.isFinal(getAnnotatedItem().getDelegate().getModifiers()))
      {
         throw new RuntimeException("Final producer method must have @Dependent scope " + annotatedMethod);
      }
   }
   
   protected void initRemoveMethod(ManagerImpl container)
   {
      Set<Method> disposalMethods = container.resolveDisposalMethods(getType(), getBindingTypes().toArray(new Annotation[0]));
      if (disposalMethods.size() == 1)
      {
         removeMethod = new InjectableMethod<Object>(disposalMethods.iterator().next());
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
      return annotatedMethod;
   }

   @Override
   protected String getDefaultName()
   {
      String propertyName = Reflections.getPropertyName(getAnnotatedItem().getDelegate());
      if (propertyName != null)
      {
         return propertyName;
      }
      else
      {
         return getAnnotatedItem().getDelegate().getName();
      }
   }

   @Override
   protected AnnotatedItem<T, Method> getXmlAnnotatedItem()
   {
      return xmlAnnotatedItem;
   }

   @Override
   protected void initType()
   {
      try
      {
         this.type = annotatedMethod.getType();
      }
      catch (ClassCastException e) 
      {
         throw new RuntimeException(getLocation() + " Cannot cast producer method return type " + annotatedMethod.getAnnotatedMethod().getReturnType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
   }
   
   private Type getDeclaredBeanType()
   {
      if (declaredBeanType == null)
      {
         Type type = getClass();
         if (type instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1)
            {
               declaredBeanType = parameterizedType.getActualTypeArguments()[0];
            }
         }
      }
      return declaredBeanType;
   }
   
   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Producer Method; declaring class: " + annotatedMethod.getAnnotatedMethod().getDeclaringClass() +"; producer method: " + annotatedMethod.getAnnotatedMethod().toString() + ";";
      }
      return location;
   }
   
   public InjectableMethod<?> getDisposalMethod()
   {
      return removeMethod;
   }
   
   public BeanModel<?, ?> getDeclaringBean()
   {
      return declaringBean;
   }

}
