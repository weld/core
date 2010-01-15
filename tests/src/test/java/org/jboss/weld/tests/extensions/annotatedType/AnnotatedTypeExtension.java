package org.jboss.weld.tests.extensions.annotatedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;

public class AnnotatedTypeExtension implements Extension
{
   
   public static class EcoFriendlyWashingMachineLiteral extends AnnotationLiteral<EcoFriendlyWashingMachine> implements EcoFriendlyWashingMachine
   {
      
      public static final EcoFriendlyWashingMachine INSTANCE = new EcoFriendlyWashingMachineLiteral();
      
   }
   
   /**
    * Adds an eco friendly wasing machine
    * @param beforeBeanDiscovery
    */
   public void addWashingMachine(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
   {
      beforeBeanDiscovery.addAnnotatedType(new AnnotatedType<WashingMachine>()
      {

         public Set<AnnotatedConstructor<WashingMachine>> getConstructors()
         {
            return Collections.emptySet();
         }

         public Set<AnnotatedField<? super WashingMachine>> getFields()
         {
            return Collections.emptySet();
         }

         public Class<WashingMachine> getJavaClass()
         {
            return WashingMachine.class;
         }

         public Set<AnnotatedMethod<? super WashingMachine>> getMethods()
         {
            return Collections.emptySet();
         }

         public <T extends Annotation> T getAnnotation(Class<T> annotationType)
         {
            if(annotationType == EcoFriendlyWashingMachine.class)
            {
               return annotationType.cast(EcoFriendlyWashingMachineLiteral.INSTANCE);
            }
            return null;
         }

         public Set<Annotation> getAnnotations()
         {
            return Collections.<Annotation>singleton(EcoFriendlyWashingMachineLiteral.INSTANCE);
         }

         public Type getBaseType()
         {
           return WashingMachine.class;
         }

         public Set<Type> getTypeClosure()
         {
            Set<Type> ret = new HashSet<Type>();
            ret.add(Object.class);
            ret.add(WashingMachine.class);
            return ret;
         }

         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
         {
            return annotationType == EcoFriendlyWashingMachine.class;
         }
         
      });
   }
}
