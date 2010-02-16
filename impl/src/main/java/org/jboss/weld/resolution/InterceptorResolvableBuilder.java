package org.jboss.weld.resolution;

import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_INTERCEPTOR_BINDING;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_BINDINGS_EMPTY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.ForbiddenArgumentException;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;

public class InterceptorResolvableBuilder extends ResolvableBuilder
{
   
   private InterceptionType interceptionType;
   
   @Override
   protected void checkQualifier(Annotation qualifier)
   {
      if (!Container.instance().services().get(MetaAnnotationStore.class).getInterceptorBindingModel(qualifier.annotationType()).isValid())
      {
         throw new ForbiddenArgumentException(INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE, qualifier);
      }
      if (qualifiers.contains(qualifier))
      {
         throw new ForbiddenArgumentException(DUPLICATE_INTERCEPTOR_BINDING, Arrays.asList(qualifiers));
      }
   }
   
   public InterceptorResolvableBuilder setInterceptionType(InterceptionType interceptionType)
   {
      this.interceptionType = interceptionType;
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifier(Annotation qualifier)
   {
      super.addQualifier(qualifier);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifiers(Annotation[] qualifiers)
   {
      super.addQualifiers(qualifiers);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addQualifiers(Set<Annotation> qualifiers)
   {
      super.addQualifiers(qualifiers);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addType(Type type)
   {
      super.addType(type);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder addTypes(Set<Type> types)
   {
      super.addTypes(types);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder setDeclaringBean(Bean<?> declaringBean)
   {
      super.setDeclaringBean(declaringBean);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder setInjectionPoint(InjectionPoint injectionPoint)
   {
      super.setInjectionPoint(injectionPoint);
      return this;
   }
   
   @Override
   public InterceptorResolvableBuilder setType(Type type)
   {
      super.setType(type);
      return this;
   }
   
   @Override
   public InterceptorResolvable create()
   {
      if (qualifiers.size() == 0)
      {
         throw new ForbiddenArgumentException(INTERCEPTOR_BINDINGS_EMPTY);
      }
      return new InterceptorResolvableImpl(rawType, types, qualifiers, mappedQualifiers, declaringBean, interceptionType);
   }
   

   private static class InterceptorResolvableImpl extends ResolvableImpl implements InterceptorResolvable
   {
      private final InterceptionType interceptionType;

      private InterceptorResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Set<Annotation> qualifiers, Map<Class<? extends Annotation>, Annotation> mappedQualifiers, Bean<?> declaringBean, InterceptionType interceptionType)
      {
         super(rawType, typeClosure, qualifiers, mappedQualifiers, declaringBean);
         this.interceptionType = interceptionType;
      }

      public InterceptionType getInterceptionType()
      {
         return interceptionType;
      }

   }

}
