package org.jboss.webbeans.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.spi.Decorator;
import javax.inject.DefinitionException;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.AnnotatedInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;

public class DecoratorBean<T> extends SimpleBean<T> implements Decorator<T>
{
   
   /**
    * Creates a decorator bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return a Bean
    */
   public static <T> DecoratorBean<T> of(AnnotatedClass<T> clazz, BeanManagerImpl manager)
   {
      return new DecoratorBean<T>(clazz, manager);
   }

   private AnnotatedItem<?, ?> decorates;
   private Set<Annotation> delegateBindings;
   private Type delegateType;
   private Set<Type> decoratedTypes;

   protected DecoratorBean(AnnotatedClass<T> type, BeanManagerImpl manager)
   {
      super(type, manager);
   }
   
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         super.initialize(environment);
         checkDecorates();
         initDecorates();
         initDecoratedTypes();
         initDelegateBindings();
         initDelegateType();
      }
   }
   
   protected void initDecoratedTypes()
   {
      this.decoratedTypes = getAnnotatedItem().getInterfaceOnlyFlattenedTypeHierarchy();
      this.decoratedTypes.remove(Serializable.class);
   }
   
   protected void initDecorates()
   {
      this.decorates = getDecoratesInjectionPoint().iterator().next();
   }
   
   @Override
   protected void checkDecorates()
   {
      for (AnnotatedInjectionPoint<?, ?> injectionPoint : getDecoratesInjectionPoint())
      {
         if (injectionPoint instanceof MethodInjectionPoint && !injectionPoint.isAnnotationPresent(Initializer.class))
         {
            throw new DefinitionException("Method with @Decorates parameter must be an initializer method " + injectionPoint);
         }
      }
      if (getDecoratesInjectionPoint().size() == 0)
      {
         throw new DefinitionException("No @Decorates injection point defined " + this);
      }
      else if (getDecoratesInjectionPoint().size() > 1)
      {
         throw new DefinitionException("Too many @Decorates injection point defined " + this);
      }
   }
   
   protected void initDelegateBindings()
   {
      this.delegateBindings = this.decorates.getBindings();
   }
   
   protected void initDelegateType()
   {
      this.delegateType = this.decorates.getType();
   }
   
   protected void checkDelegateType()
   {
      for (Type decoratedType : getDecoratedTypes())
      {
         if (decoratedType instanceof Class)
         {
            if (!((Class<?>) decoratedType).isAssignableFrom(decorates.getRawType()))
            {
               throw new DefinitionException("The delegate type must extend or implement every decorated type. Decorated type " + decoratedType + "." + this );
            }
         }
         else if (decoratedType instanceof ParameterizedType)
         {
            ParameterizedType parameterizedType = (ParameterizedType) decoratedType;
            if (!decorates.isParameterizedType())
            {
               throw new DefinitionException("The decorated type is parameterized, but the delegate type isn't. Delegate type " + delegateType + "." + this);
            }
            if (!Arrays.equals(decorates.getActualTypeArguments(), parameterizedType.getActualTypeArguments()));
            Type rawType = ((ParameterizedType) decoratedType).getRawType();
            if (rawType instanceof Class && !((Class<?>) rawType).isAssignableFrom(decorates.getRawType()))
            {
               throw new DefinitionException("The delegate type must extend or implement every decorated type. Decorated type " + decoratedType + "." + this );
            }
            else
            {
               throw new IllegalStateException("Unable to process " + decoratedType);
            }

         }
      }
   }

   public Set<Annotation> getDelegateBindings()
   {
      return delegateBindings;
   }

   public Type getDelegateType()
   {
      return delegateType;
   }

   public Set<Type> getDecoratedTypes()
   {
      return decoratedTypes;
   }

}
