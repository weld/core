/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_DEFAULT_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_INJECTABLE_CONSTRUCTORS;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_INJECTABLE_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_POST_CONSTRUCT_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_ONE_PRE_DESTROY_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_POST_CONSTRUCT_METHODS;
import static org.jboss.weld.logging.messages.BeanMessage.FOUND_PRE_DESTROY_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.AMBIGUOUS_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.UtilMessage.ANNOTATION_NOT_QUALIFIER;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_CANNOT_BE_DISPOSAL_METHOD;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_CANNOT_BE_OBSERVER;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_CANNOT_BE_PRODUCER;
import static org.jboss.weld.logging.messages.UtilMessage.INITIALIZER_METHOD_IS_GENERIC;
import static org.jboss.weld.logging.messages.UtilMessage.INVALID_QUANTITY_INJECTABLE_FIELDS_AND_INITIALIZER_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.QUALIFIER_ON_FINAL_FIELD;
import static org.jboss.weld.logging.messages.UtilMessage.REDUNDANT_QUALIFIER;
import static org.jboss.weld.logging.messages.UtilMessage.TOO_MANY_POST_CONSTRUCT_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.TOO_MANY_PRE_DESTROY_METHODS;
import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_CONSTRUCTOR;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.decorator.Decorator;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.interceptor.model.InterceptionType;
import org.jboss.interceptor.model.InterceptionTypeRegistry;
import org.jboss.weld.Container;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.ForbiddenArgumentException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Helper class for bean inspection
 * 
 * @author Pete Muir
 * @author David Allen
 * @author Marius Bogoevici
 *
 */
public class Beans
{
   // TODO Convert messages
   private static final LocLogger log = loggerFactory().getLogger(BEAN);
   
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
      if (bean instanceof RIBean<?>)
      {
         return ((RIBean<?>) bean).isPassivationCapableBean();
      }
      else
      {
         return Reflections.isSerializable(bean.getBeanClass());
      }
   }
   
   /**
    * Tests if a bean is capable of having its state temporarily stored to
    * secondary storage
    * 
    * @param bean The bean to inspect
    * @return True if the bean is passivation capable
    */
   public static boolean isPassivationCapableDependency(Bean<?> bean)
   {
      if (bean instanceof RIBean<?>)
      {
         return ((RIBean<?>) bean).isPassivationCapableDependency();
      }
      else
      {
         if (Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal())
         {
            return true;
         }
         else if (bean.getScope().equals(Dependent.class) && isPassivationCapableBean(bean))
         {
            return true;
         }
         else
         {
            return false;
         }
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
         for (WeldField<?, ?> annotatedField : t.getDeclaredWeldFields(Inject.class))
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
   
   public static <T> List<WeldMethod<?, ? super T>> getPostConstructMethods(WeldClass<T> type)
   {
      WeldClass<?> t = type;
      List<WeldMethod<?, ? super T>> methods = new ArrayList<WeldMethod<?, ? super T>>();
      while (!t.getJavaClass().equals(Object.class))
      {
         Set<WeldMethod<?, ? super T>> declaredMethods = (Set) t.getDeclaredWeldMethods(PostConstruct.class);
         log.trace(FOUND_POST_CONSTRUCT_METHODS, declaredMethods, type);
         if (declaredMethods.size() > 1)
         {
            throw new DefinitionException(TOO_MANY_POST_CONSTRUCT_METHODS, type);
         }
         else if (declaredMethods.size() == 1)
         {
            WeldMethod<?, ? super T> method = declaredMethods.iterator().next();
            log.trace(FOUND_ONE_POST_CONSTRUCT_METHOD, method, type);
            methods.add(0, method);
         }
         t = t.getWeldSuperclass();
      }
      return methods;
   }
   
   public static <T> List<WeldMethod<?, ? super T>> getPreDestroyMethods(WeldClass<T> type)
   {
      WeldClass<?> t = type;
      List<WeldMethod<?, ? super T>> methods = new ArrayList<WeldMethod<?, ? super T>>();
      while (!t.getJavaClass().equals(Object.class))
      {
         Set<WeldMethod<?, ? super T>> declaredMethods = (Set) t.getDeclaredWeldMethods(PreDestroy.class);
         log.trace(FOUND_PRE_DESTROY_METHODS, declaredMethods, type);
         if (declaredMethods.size() > 1)
         {
            throw new DefinitionException(TOO_MANY_PRE_DESTROY_METHODS, type);
         }
         else if (declaredMethods.size() == 1)
         {
            WeldMethod<?, ? super T> method = declaredMethods.iterator().next();
            log.trace(FOUND_ONE_PRE_DESTROY_METHOD, method, type);
            methods.add(0, method);
         }
         t = t.getWeldSuperclass();
      }
      return methods;
   }

   public static List<WeldMethod<?,?>> getInterceptableMethods(WeldClass<?> type)
   {
      List<WeldMethod<?, ?>> annotatedMethods = new ArrayList<WeldMethod<?, ?>>();
      for (WeldMethod<?, ?> annotatedMethod : type.getWeldMethods())
      {
         int modifiers = ((WeldMember) annotatedMethod).getJavaMember().getModifiers();
         boolean businessMethod = !annotatedMethod.isStatic()
               && !annotatedMethod.isAnnotationPresent(Inject.class);

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
         for (WeldField<?, ?> field : type.getWeldFields(ejbAnnotationType))
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
         for (WeldField<?, ?> field : type.getWeldFields(persistenceContextAnnotationType))
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
         for (WeldField<?, ?> field : type.getWeldFields(persistenceUnitAnnotationType))
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
         for (WeldField<?, ?> field : type.getWeldFields(resourceAnnotationType))
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
                  throw new DefinitionException(INITIALIZER_CANNOT_BE_PRODUCER, method, type);
               }
               else if (method.getWeldParameters(Disposes.class).size() > 0)
               {
                  throw new DefinitionException(INITIALIZER_CANNOT_BE_DISPOSAL_METHOD, method, type);
               }
               else if (method.getWeldParameters(Observes.class).size() > 0)
               {
                  throw new DefinitionException(INITIALIZER_CANNOT_BE_OBSERVER, method, type);
               }
               else if (method.getJavaMember().getTypeParameters().length > 0)
               {
                  throw new DefinitionException(INITIALIZER_METHOD_IS_GENERIC, method, type);
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
      for (WeldParameter<?, ?> parameter : constructor.getWeldParameters())
      {
         injectionPoints.add(ParameterInjectionPoint.of(declaringBean, parameter));
      }
      return injectionPoints;
   }
   
   public static Set<ParameterInjectionPoint<?, ?>> getParameterInjectionPoints(Bean<?> declaringBean, MethodInjectionPoint<?, ?> method)
   {
      Set<ParameterInjectionPoint<?,?>> injectionPoints = new HashSet<ParameterInjectionPoint<?,?>>();
      for (WeldParameter<?, ?> parameter : method.getWeldParameters())
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
            for (WeldParameter<?, ?> parameter : method.getWeldParameters())
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
            throw new DefinitionException(QUALIFIER_ON_FINAL_FIELD, annotatedField);
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
         QualifierModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getBindingTypeModel(binding.annotationType());
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

   public static boolean findInterceptorBindingConflicts(BeanManagerImpl manager, Set<Annotation> bindings)
   {
      Map<Class<? extends Annotation>, Annotation> foundAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      for (Annotation binding: bindings)
      {
         if (foundAnnotations.containsKey(binding.annotationType()))
         {
            InterceptorBindingModel<?> bindingType = manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(binding.annotationType());
            if (!bindingType.isEqual(binding, foundAnnotations.get(binding.annotationType()), false))
            {
                return true;
            }
         }
         else
         {
            foundAnnotations.put(binding.annotationType(), binding);
         }
      }
      return false;      
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
   public static <T extends Bean<?>> Set<T> removeDisabledAndSpecializedBeans(Set<T> beans, Collection<Class<?>> enabledAlternativeClasses, Collection<Class<? extends Annotation>> enabledAlternativeSterotypes, Map<Contextual<?>, Contextual<?>> specializedBeans)
   {
      if (beans.size() == 0)
      {
         return beans;
      }
      else
      {
         Set<T> result = new HashSet<T>();
         for (T bean : beans)
         {
            if (isBeanEnabled(bean, enabledAlternativeClasses, enabledAlternativeSterotypes) && !isSpecialized(bean, beans, specializedBeans))
            {
               result.add(bean);
            }
         }
         return result;
      }
   }
   
   public static boolean isBeanEnabled(Bean<?> bean, Collection<Class<?>> enabledAlternativeClasses, Collection<Class<? extends Annotation>> enabledAlternativeSterotypes)
   {
      if (bean.isAlternative())
      {
         if (enabledAlternativeClasses.contains(bean.getBeanClass()))
         {
            return true;
         }
         else
         {
            for (Class<? extends Annotation> stereotype : bean.getStereotypes())
            {
               if (enabledAlternativeSterotypes.contains(stereotype))
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
    * Check if any of the beans is an alternative
    * 
    * @param beans
    * @return
    */
   public static boolean isAlternativePresent(Set<Bean<?>> beans)
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
   
   public static boolean isAlternative(WeldAnnotated<?, ?> annotated, MergedStereotypes<?, ?> mergedStereotypes)
   {
      if (annotated.isAnnotationPresent(Alternative.class))
      {
         return true;
      }
      else
      {
         return mergedStereotypes.isAlternative();
      }
   }
   
   /**
    * Check if bean is specialized by any of beans
    * 
    * @param bean
    * @param beans
    * @param specializedBeans
    * @return
    */
   public static <T extends Bean<?>> boolean isSpecialized(T bean, Set<T> beans, Map<Contextual<?>, Contextual<?>> specializedBeans)
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
      Set<WeldConstructor<T>> initializerAnnotatedConstructors = type.getWeldConstructors(Inject.class);
      log.trace(FOUND_INJECTABLE_CONSTRUCTORS, initializerAnnotatedConstructors, type);
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException(AMBIGUOUS_CONSTRUCTOR, type, initializerAnnotatedConstructors);
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         constructor = ConstructorInjectionPoint.of(declaringBean, initializerAnnotatedConstructors.iterator().next());
         log.trace(FOUND_ONE_INJECTABLE_CONSTRUCTOR, constructor, type);
      }
      else if (type.getNoArgsWeldConstructor() != null)
      {

         constructor = ConstructorInjectionPoint.of(declaringBean, type.getNoArgsWeldConstructor());
         log.trace(FOUND_DEFAULT_CONSTRUCTOR, constructor, type);
      }
      
      if (constructor == null)
      {
         throw new DefinitionException(UNABLE_TO_FIND_CONSTRUCTOR, type);
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
         throw new ForbiddenArgumentException(INVALID_QUANTITY_INJECTABLE_FIELDS_AND_INITIALIZER_METHODS, injectableFields, initializerMethods);  
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
   
   public static Annotation[] mergeInQualifiers(Annotation[] qualifiers, Annotation[] newQualifiers)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      result.addAll(Arrays.asList(qualifiers));
      Set<Annotation> checkedNewQualifiers = new HashSet<Annotation>();
      for (Annotation qualifier : newQualifiers)
      {
         if (!Container.instance().services().get(MetaAnnotationStore.class).getBindingTypeModel(qualifier.annotationType()).isValid())
         {
            throw new ForbiddenArgumentException(ANNOTATION_NOT_QUALIFIER, qualifier);
         }
         if (checkedNewQualifiers.contains(qualifier))
         {
            throw new ForbiddenArgumentException(REDUNDANT_QUALIFIER, qualifier, Arrays.asList(newQualifiers));
         }
         checkedNewQualifiers.add(qualifier);
      }
      result.addAll(checkedNewQualifiers);
      return result.toArray(Reflections.EMPTY_ANNOTATIONS);
   }

   public static InjectionPoint getDelegateInjectionPoint(javax.enterprise.inject.spi.Decorator<?> decorator)
   {
      if (decorator instanceof DecoratorImpl<?>)
      {
         return ((DecoratorImpl<?>)decorator).getDelegateInjectionPoint();
      }
      else
      {
         for (InjectionPoint injectionPoint: decorator.getInjectionPoints())
         {
            if (injectionPoint.isDelegate())
               return injectionPoint;
         }
      }
      return null;
   }
}
