/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.decorator.Decorates;
import javax.decorator.Decorator;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.jboss.interceptor.model.InterceptionType;
import org.jboss.interceptor.model.InterceptionTypeRegistry;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.log.Log;
import org.jboss.weld.log.Logging;
import org.jboss.weld.metadata.cache.BindingTypeModel;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.persistence.PersistenceApiAbstraction;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Helper class for bean inspection
 * 
 * @author Pete Muir
 * @author David Allen
 *
 */
public class Beans
{
   
   private static final Log log = Logging.getLog(Beans.class);
   
   /**
    * Indicates if a bean's scope type is passivating
    * 
    * @param bean The bean to inspect
    * @return True if the scope is passivating, false otherwise
    */
   public static boolean isPassivatingScope(Bean<?> bean, BeanManagerImpl manager)
   {
      if (bean instanceof SessionBean<?>)
      {
         return ((SessionBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else
      {
         return manager.getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isPassivating();
      }
   }

   /**
    * Tests if a bean is capable of having its state temporarily stored to
    * secondary storage
    * 
    * @param bean The bean to inspect
    * @return True if the bean is passivation capable
    */
   public static boolean isPassivationCapableBean(Bean<?> bean)
   {
      if (bean instanceof SessionBean<?>)
      {
         return ((SessionBean<?>) bean).getEjbDescriptor().isStateful();
      }
      else if (bean instanceof AbstractProducerBean<?, ?, ?>)
      {
         return Reflections.isSerializable(((AbstractProducerBean<?, ?, ?>) bean).getType());
      }
      else
      {
         return Reflections.isSerializable(bean.getBeanClass());
      }
   }

   /**
    * Indicates if a bean is proxyable
    * 
    * @param bean The bean to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isBeanProxyable(Bean<?> bean)
   {
      if (bean instanceof RIBean<?>)
      {
         return ((RIBean<?>) bean).isProxyable();
      }
      else
      {
         return Proxies.isTypesProxyable(bean.getTypes());
      }
   }

   public static List<Set<FieldInjectionPoint<?, ?>>> getFieldInjectionPoints(Bean<?> declaringBean, WeldClass<?> type)
   {
      List<Set<FieldInjectionPoint<?, ?>>> injectableFieldsList = new ArrayList<Set<FieldInjectionPoint<?, ?>>>();
      WeldClass<?> t = type;
      while (!t.getJavaClass().equals(Object.class))
      {
         Set<FieldInjectionPoint<?, ?>> fields = new HashSet<FieldInjectionPoint<?,?>>();
         injectableFieldsList.add(0, fields);
         for (WeldField<?, ?> annotatedField : t.getDeclaredAnnotatedWeldFields(Inject.class))
         {
            if (!annotatedField.isStatic())
            {
               addFieldInjectionPoint(annotatedField, fields, declaringBean);
            }
         }
         for (WeldField<?, ?> annotatedField : t.getAnnotatedWeldFields(Decorates.class))
         {
            if (!annotatedField.isStatic())
            {
               addFieldInjectionPoint(annotatedField, fields, declaringBean);
            }
         }
         t = t.getWeldSuperclass();
      }
      return injectableFieldsList;
   }
   
   public static Set<FieldInjectionPoint<?, ?>> getFieldInjectionPoints(Bean<?> declaringBean, List<? extends Set<? extends FieldInjectionPoint<?, ?>>> fieldInjectionPoints)
   {
      Set<FieldInjectionPoint<?, ?>> injectionPoints = new HashSet<FieldInjectionPoint<?,?>>();
      for (Set<? extends FieldInjectionPoint<?, ?>> i : fieldInjectionPoints)
      {
         injectionPoints.addAll(i);
      }
      return injectionPoints;
   }
   
   public static WeldMethod<?, ?> getPostConstruct(WeldClass<?> type)
   {
      Set<WeldMethod<?, ?>> postConstructMethods = type.getAnnotatedWeldMethods(PostConstruct.class);
      log.trace("Found " + postConstructMethods + " constructors annotated with @Initializer for " + type);
      if (postConstructMethods.size() > 1)
      {
         throw new DefinitionException("Cannot have more than one post construct method annotated with @PostConstruct for " + type);
      }
      else if (postConstructMethods.size() == 1)
      {
         WeldMethod<?, ?> postConstruct = postConstructMethods.iterator().next();
         log.trace("Exactly one post construct method (" + postConstruct + ") for " + type);
         return postConstruct;
      }
      else
      {
         return null;
      }
   }
   
   public static WeldMethod<?, ?> getPreDestroy(WeldClass<?> type)
   {
      Set<WeldMethod<?, ?>> preDestroyMethods = type.getAnnotatedWeldMethods(PreDestroy.class);
      log.trace("Found " + preDestroyMethods + " constructors annotated with @Initializer for " + type);
      if (preDestroyMethods.size() > 1)
      {
         // TODO actually this is wrong, in EJB you can have @PreDestroy methods
         // on the superclass, though the CDI spec is silent on the issue
         throw new DefinitionException("Cannot have more than one pre destroy method annotated with @PreDestroy for " + type);
      }
      else if (preDestroyMethods.size() == 1)
      {
         WeldMethod<?, ?> preDestroy = preDestroyMethods.iterator().next();
         log.trace("Exactly one post construct method (" + preDestroy + ") for " + type);
         return preDestroy;
      }
      else
      {
         return null;
      }
   }

   public static List<WeldMethod<?,?>> getInterceptableBusinessMethods(WeldClass<?> type)
   {
      List<WeldMethod<?, ?>> annotatedMethods = new ArrayList<WeldMethod<?, ?>>();
      for (WeldMethod<?, ?> annotatedMethod : type.getWeldMethods())
      {
         int modifiers = ((WeldMember) annotatedMethod).getJavaMember().getModifiers();
         boolean businessMethod = !annotatedMethod.isStatic()
               && (Modifier.isPublic(modifiers)
                  || Modifier.isProtected(modifiers))
               && !annotatedMethod.isAnnotationPresent(Inject.class)
               && !annotatedMethod.isAnnotationPresent(Produces.class)
               && annotatedMethod.getAnnotatedWBParameters(Disposes.class).isEmpty()
               && annotatedMethod.getAnnotatedWBParameters(Observes.class).isEmpty();

         if (businessMethod)
         {
            for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes())
            {
               businessMethod = !annotatedMethod.isAnnotationPresent(InterceptionTypeRegistry.getAnnotationClass(interceptionType));
               if (!businessMethod)
                  break;
            }
            if (businessMethod)
               annotatedMethods.add(annotatedMethod);
         }
      }
      return annotatedMethods;
   }
   

   public static Set<WeldInjectionPoint<?, ?>> getEjbInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager)
   {
      if (manager.getServices().contains(EjbInjectionServices.class))
      {
         Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
         Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints = new HashSet<WeldInjectionPoint<?, ?>>();
         for (WeldField<?, ?> field : type.getAnnotatedWeldFields(ejbAnnotationType))
         {
            ejbInjectionPoints.add(FieldInjectionPoint.of(declaringBean, field));
         }
         return ejbInjectionPoints;
      }
      else
      {
         return Collections.emptySet();
      }
   }

   public static Set<WeldInjectionPoint<?, ?>> getPersistenceContextInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager)
   {
      if (manager.getServices().contains(JpaInjectionServices.class))
      {
         Set<WeldInjectionPoint<?, ?>> jpaInjectionPoints = new HashSet<WeldInjectionPoint<?, ?>>();
         Class<? extends Annotation> persistenceContextAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
         for (WeldField<?, ?> field : type.getAnnotatedWeldFields(persistenceContextAnnotationType))
         {
            jpaInjectionPoints.add(FieldInjectionPoint.of(declaringBean, field));
         }
         return jpaInjectionPoints;
      }
      else
      {
         return Collections.emptySet();
      }
   }
   
   public static Set<WeldInjectionPoint<?, ?>> getPersistenceUnitInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager)
   {
      if (manager.getServices().contains(JpaInjectionServices.class))
      {
         Set<WeldInjectionPoint<?, ?>> jpaInjectionPoints = new HashSet<WeldInjectionPoint<?, ?>>();
         Class<? extends Annotation> persistenceUnitAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_UNIT_ANNOTATION_CLASS;
         for (WeldField<?, ?> field : type.getAnnotatedWeldFields(persistenceUnitAnnotationType))
         {
            jpaInjectionPoints.add(FieldInjectionPoint.of(declaringBean, field));
         }
         return jpaInjectionPoints;
      }
      else
      {
         return Collections.emptySet();
      }
   }

   public static Set<WeldInjectionPoint<?, ?>> getResourceInjectionPoints(Bean<?> declaringBean, WeldClass<?> type, BeanManagerImpl manager)
   {
      if (manager.getServices().contains(ResourceInjectionServices.class))
      {
         Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
         Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints = new HashSet<WeldInjectionPoint<?, ?>>();
         for (WeldField<?, ?> field : type.getAnnotatedWeldFields(resourceAnnotationType))
         {
            resourceInjectionPoints.add(FieldInjectionPoint.of(declaringBean, field));
         }
         return resourceInjectionPoints;
      }
      else
      {
         return Collections.emptySet();
      }
   }
   
   public static List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods(Bean<?> declaringBean, WeldClass<?> type)
   {
      List<Set<MethodInjectionPoint<?, ?>>> initializerMethodsList = new ArrayList<Set<MethodInjectionPoint<?, ?>>>();
      // Keep track of all seen methods so we can ignore overridden methods
      Multimap<MethodSignature, Package> seenMethods = Multimaps.newSetMultimap(new HashMap<MethodSignature, Collection<Package>>(), new Supplier<Set<Package>>()
      {

         public Set<Package> get()
         {
            return new HashSet<Package>();
         }
         
      });
      WeldClass<?> t = type;
      while (!t.getJavaClass().equals(Object.class))
      {
         Set<MethodInjectionPoint<?, ?>> initializerMethods = new HashSet<MethodInjectionPoint<?,?>>();
         initializerMethodsList.add(0, initializerMethods);
         for (WeldMethod<?, ?> method : t.getDeclaredWeldMethods())
         {
            if (method.isAnnotationPresent(Inject.class) && !method.isStatic())
            {
               if (method.getAnnotation(Produces.class) != null)
               {
                  throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Produces on " + type);
               }
               else if (method.getAnnotatedWBParameters(Disposes.class).size() > 0)
               {
                  throw new DefinitionException("Initializer method " + method.toString() + " cannot have parameters annotated @Disposes on " + type);
               }
               else if (method.getAnnotatedWBParameters(Observes.class).size() > 0)
               {
                  throw new DefinitionException("Initializer method " + method.toString() + " cannot be annotated @Observes on " + type);
               }
               else
               {
                  if (!isOverridden(method, seenMethods))
                  {
                     MethodInjectionPoint<?, ?> initializerMethod = MethodInjectionPoint.of(declaringBean, method); 
                     initializerMethods.add(initializerMethod);
                  }
               }
            }
            seenMethods.put(method.getSignature(), method.getPackage());
         }
         t = t.getWeldSuperclass();
      }
      return initializerMethodsList;
   }
   
   private static boolean isOverridden(WeldMethod<?, ?> method, Multimap<MethodSignature, Package> seenMethods)
   {
      if (method.isPrivate())
      {
         return false;
      }
      else if (method.isPackagePrivate() && seenMethods.containsKey(method.getSignature()))
      {
         return seenMethods.get(method.getSignature()).contains(method.getPackage());
      }
      else
      {
         return seenMethods.containsKey(method.getSignature());
      }
   }
   
   public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(Bean<?> declaringBean, WeldConstructor<?> constructor)
   {
      Set<ParameterInjectionPoint<?,?>> injectionPoints = new HashSet<ParameterInjectionPoint<?,?>>();
      for (WeldParameter<?, ?> parameter : constructor.getWBParameters())
      {
         injectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
      }
      return injectionPoints;
   }
   
   public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(Bean<?> declaringBean, List<Set<MethodInjectionPoint<?, ?>>> methodInjectionPoints)
   {
      Set<ParameterInjectionPoint<?, ?>> injectionPoints = new HashSet<ParameterInjectionPoint<?,?>>();
      for (Set<MethodInjectionPoint<?, ?>> i : methodInjectionPoints)
      {
         for (MethodInjectionPoint<?, ?> method : i)
         {
            for (WeldParameter<?, ?> parameter : method.getWBParameters())
            {
               injectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
            }
         }
      }
      return injectionPoints;
   }
   
   private static void addFieldInjectionPoint(WeldField<?, ?> annotatedField, Set<FieldInjectionPoint<?, ?>> injectableFields, Bean<?> declaringBean)
   {
      if (!annotatedField.isAnnotationPresent(Produces.class))
      {
         if (annotatedField.isFinal())
         {
            throw new DefinitionException("Don't place binding annotations on final fields " + annotatedField);
         }
         FieldInjectionPoint<?, ?> fieldInjectionPoint = FieldInjectionPoint.of(declaringBean, annotatedField);
         injectableFields.add(fieldInjectionPoint);
      }
   }
   
   /**
    * Checks if binding criteria fulfill all binding types
    * 
    * @param element The binding criteria to check
    * @param bindings2 The binding types to check
    * @return True if all matches, false otherwise
    */
   public static boolean containsAllBindings(Set<Annotation> bindings1, Set<Annotation> bindings2, BeanManagerImpl manager)
   {
      for (Annotation binding : bindings1)
      {
         BindingTypeModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getBindingTypeModel(binding.annotationType());
         boolean matchFound = false;
         // TODO Something wrong with annotation proxy hashcode in JDK/AnnotationLiteral hashcode, so always do a full check, don't use contains
         for (Annotation otherBinding : bindings2)
         {
            if (bindingType.isEqual(binding, otherBinding))
            {
               matchFound = true;
            }
         }
         if (!matchFound)
         {
            return false;
         }
      }
      return true;
   }

   public static boolean containsAllInterceptionBindings(Set<Annotation> expectedBindings, Set<Annotation> existingBindings, BeanManagerImpl manager)
   {
      for (Annotation binding : expectedBindings)
      {
         InterceptorBindingModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(binding.annotationType());
         boolean matchFound = false;
         // TODO Something wrong with annotation proxy hashcode in JDK/AnnotationLiteral hashcode, so always do a full check, don't use contains
         for (Annotation otherBinding : existingBindings)
         {
            if (bindingType.isEqual(binding, otherBinding))
            {
               matchFound = true;
            }
         }
         if (!matchFound)
         {
            return false;
         }
      }
      return true;
   }
   

   /**
    * Retains only beans which have deployment type X.
    * 
    * The deployment type X is
    * 
    * @param <T>
    * @param beans The beans to filter
    * @param enabledDeploymentTypes The enabled deployment types
    * @return The filtered beans
    */
   public static <T extends Bean<?>> Set<T> retainEnabledPolicies(Set<T> beans, Collection<Class<?>> enabledPolicyClasses, Collection<Class<? extends Annotation>> enabledPolicySterotypes)
   {
      if (beans.size() == 0)
      {
         return beans;
      }
      else
      {
         Set<T> enabledBeans = new HashSet<T>();
         for (T bean : beans)
         {
            if (isBeanEnabled(bean, enabledPolicyClasses, enabledPolicySterotypes))
            {
               enabledBeans.add(bean);
            }
         }
         return enabledBeans;
      }
   }
   
   public static boolean isBeanEnabled(Bean<?> bean, Collection<Class<?>> enabledPolicyClasses, Collection<Class<? extends Annotation>> enabledPolicySterotypes)
   {
      if (bean.isAlternative())
      {
         if (enabledPolicyClasses.contains(bean.getBeanClass()))
         {
            return true;
         }
         else
         {
            for (Class<? extends Annotation> stereotype : bean.getStereotypes())
            {
               if (enabledPolicySterotypes.contains(stereotype))
               {
                  return true;
               }
            }
         }
      }
      else
      {
         return true;
      }
      return false;
   }
   
   /**
    * Check if any of the beans is a policy
    * 
    * @param beans
    * @return
    */
   public static <X> boolean isPolicyPresent(Set<Bean<? extends X>> beans)
   {
      for (Bean<?> bean : beans)
      {
         if (bean.isAlternative())
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Check if bean is specialized by any of beans
    * 
    * @param bean
    * @param beans
    * @param specializedBeans
    * @return
    */
   public static <X> boolean isSpecialized(Bean<? extends X> bean, Set<Bean<? extends X>> beans, Map<Contextual<?>, Contextual<?>> specializedBeans)
   {
      if (specializedBeans.containsKey(bean))
      {
         if (beans.contains(specializedBeans.get(bean)))
         {
            return true;
         }
      }
      return false;
   }
   
   public static <T> ConstructorInjectionPoint<T> getBeanConstructor(Bean<T> declaringBean, WeldClass<T> type)
   {
      ConstructorInjectionPoint<T> constructor = null;
      Set<WeldConstructor<T>> initializerAnnotatedConstructors = type.getAnnotatedWeldConstructors(Inject.class);
      log.trace("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + type);
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + type);
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         constructor = ConstructorInjectionPoint.of(declaringBean, initializerAnnotatedConstructors.iterator().next());
         log.trace("Exactly one constructor (" + constructor + ") annotated with @Initializer defined, using it as the bean constructor for " + type);
      }
      else if (type.getNoArgsWeldConstructor() != null)
      {

         constructor = ConstructorInjectionPoint.of(declaringBean, type.getNoArgsWeldConstructor());
         log.trace("Exactly one constructor (" + constructor + ") defined, using it as the bean constructor for " + type);
      }
      
      if (constructor == null)
      {
         throw new DefinitionException("Cannot determine constructor to use for " + type);
      }
      else
      {
         return constructor;
      }
   }
   
   /**
    * Injects EJBs and common fields
    */
   public static <T> void injectEEFields(T beanInstance, BeanManagerImpl manager, Iterable<WeldInjectionPoint<?, ?>> ejbInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints, Iterable<WeldInjectionPoint<?, ?>> resourceInjectionPoints)
   {
      EjbInjectionServices ejbServices = manager.getServices().get(EjbInjectionServices.class);
      JpaInjectionServices jpaServices = manager.getServices().get(JpaInjectionServices.class);
      ResourceInjectionServices resourceServices = manager.getServices().get(ResourceInjectionServices.class);
      
      if (ejbServices != null)
      {
         for (WeldInjectionPoint<?, ?> injectionPoint : ejbInjectionPoints)
         {
            Object ejbInstance = ejbServices.resolveEjb(injectionPoint);
            injectionPoint.inject(beanInstance, ejbInstance);
         }
      }

      if (jpaServices != null)
      {
         for (WeldInjectionPoint<?, ?> injectionPoint : persistenceContextInjectionPoints)
         {
            Object pcInstance = jpaServices.resolvePersistenceContext(injectionPoint);
            injectionPoint.inject(beanInstance, pcInstance);
         }
         for (WeldInjectionPoint<?, ?> injectionPoint : persistenceUnitInjectionPoints)
         {
            Object puInstance = jpaServices.resolvePersistenceUnit(injectionPoint);
            injectionPoint.inject(beanInstance, puInstance);
         }
      }

      if (resourceServices != null)
      {
         for (WeldInjectionPoint<?, ?> injectionPoint : resourceInjectionPoints)
         {
            Object resourceInstance = resourceServices.resolveResource(injectionPoint);
            injectionPoint.inject(beanInstance, resourceInstance);
         }
      }
   }
   

   /**
    * Gets the declared bean type
    * 
    * @return The bean type
    */
   public static Type getDeclaredBeanType(Class<? extends Bean> clazz)
   {
      Type[] actualTypeArguments = Reflections.getActualTypeArguments(clazz);
      if (actualTypeArguments.length == 1)
      {
         return actualTypeArguments[0];
      }
      else
      {
         return null;
      }
   }
   

   /**
    * Injects bound fields
    * 
    * @param instance The instance to inject into
    */
   public static <T> void injectBoundFields(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager, Iterable<? extends FieldInjectionPoint<?, ?>> injectableFields)
   {
      for (FieldInjectionPoint<?, ?> injectableField : injectableFields)
      {
         injectableField.inject(instance, manager, creationalContext);
      }
   }
   
   public static<T> void injectFieldsAndInitializers(T instance, CreationalContext<T> ctx, BeanManagerImpl beanManager, List<? extends Iterable<? extends FieldInjectionPoint<?, ?>>> injectableFields, List<? extends Iterable<? extends MethodInjectionPoint<?, ?>>>initializerMethods)
   {
      if (injectableFields.size() != initializerMethods.size())
      {
         throw new IllegalArgumentException("injectableFields and initializerMethods must have the same size. InjectableFields: " + injectableFields + "; InitializerMethods: " + initializerMethods);  
      }
      for (int i = 0; i < injectableFields.size(); i++)
      {
         injectBoundFields(instance, ctx, beanManager, injectableFields.get(i));
         callInitializers(instance, ctx, beanManager, initializerMethods.get(i));
      }
   }
   
   /**
    * Calls all initializers of the bean
    * 
    * @param instance The bean instance
    */
   public static <T> void callInitializers(T instance, CreationalContext<T> creationalContext, BeanManagerImpl manager, Iterable<? extends MethodInjectionPoint<?, ?>> initializerMethods)
   {
      for (MethodInjectionPoint<?, ?> initializer : initializerMethods)
      {
         initializer.invoke(instance, manager, creationalContext, CreationException.class);
      }
   }

   public static <T> boolean isInterceptor(WeldClass<T> annotatedItem)
   {
      return annotatedItem.isAnnotationPresent(javax.interceptor.Interceptor.class);
   }

   public static <T> boolean isDecorator(WeldClass<T> annotatedItem)
   {
      return annotatedItem.isAnnotationPresent(Decorator.class);
   }
   
}
