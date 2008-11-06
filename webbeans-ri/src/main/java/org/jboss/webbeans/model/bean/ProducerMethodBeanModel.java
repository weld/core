package org.jboss.webbeans.model.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.Observes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.MethodConstructor;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Reflections;

public class ProducerMethodBeanModel<T> extends AbstractBeanModel<T, Method>
{
   
   private MethodConstructor<T> constructor;
   
   private AnnotatedItem<T, Method> xmlAnnotatedItem;
   private AnnotatedMethod<T> annotatedMethod;
   
   private BeanModel<?, ?> declaringBean;
   
   // Cached values
   private String location;
   private Type declaredBeanType;
   
   public ProducerMethodBeanModel(AnnotatedMethod<T> annotatedMethod, AnnotatedMethod<T> xmlAnnotatedMethod, ManagerImpl manager)
   {
      this.annotatedMethod = annotatedMethod;
      this.xmlAnnotatedItem = xmlAnnotatedMethod;
      init(manager);
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
   protected void initDeploymentType()
   {
      super.initDeploymentType();
      if (getDeploymentType() == null)
      {
         if (getDeclaringBean() == null)
         {
            initDeclaringBean(manager);
         }
         deploymentType = declaringBean.getDeploymentType();
      }
   }

   protected void initDeclaringBean(ManagerImpl container)
   {
      declaringBean = container.getModelManager().getBeanModel(getAnnotatedItem().getDelegate().getDeclaringClass());
   }
   
   public MethodConstructor<T> getConstructor()
   {
      return constructor;
   }
   
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().isStatic())
      {
         throw new DefinitionException("Producer method cannot be static " + annotatedMethod);
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
      // TODO Don't use delegate here
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
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e) 
      {
         throw new RuntimeException(getLocation() + " Cannot cast producer method return type " + annotatedMethod.getAnnotatedMethod().getReturnType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
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
