package org.jboss.webbeans.model.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.webbeans.injectable.BeanConstructor;
import org.jboss.webbeans.injectable.Injectable;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedItem;

public interface BeanModel<T, E>
{
   
   public Set<Annotation> getBindingTypes();
   
   public Class<? extends Annotation> getScopeType();
   
   public Class<? extends T> getType();
   
   public Set<Class<?>> getApiTypes();
   
   public BeanConstructor<T> getConstructor();
   
   /**
    * Convenience method that return's the bean's "location" for logging
    * and exception throwing
    */
   public String getLocation();
   
   public Class<? extends Annotation> getDeploymentType();
   
   public String getName();
   
   public InjectableMethod<?> getRemoveMethod();
   
   public Set<Injectable<?, ?>> getInjectionPoints();
   
   public boolean isAssignableFrom(AnnotatedItem<?, ?> annotatedItem);
   
}