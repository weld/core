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
package org.jboss.weld.manager;

import static org.jboss.weld.logging.messages.BeanManagerMessage.AMBIGUOUS_BEANS_FOR_DEPENDENCY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.CONTEXT_NOT_ACTIVE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_ACTIVE_CONTEXTS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_INTERCEPTOR_BINDING;
import static org.jboss.weld.logging.messages.BeanManagerMessage.DUPLICATE_QUALIFIERS;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_BINDINGS_EMPTY;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.INVALID_QUALIFIER;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NON_NORMAL_SCOPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_INTERCEPTOR_BINDING_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_PROXYABLE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NOT_STEREOTYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.NO_DECORATOR_TYPES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.SPECIFIED_TYPE_NOT_BEAN_TYPE;
import static org.jboss.weld.logging.messages.BeanManagerMessage.TOO_MANY_ACTIVITIES;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNPROXYABLE_RESOLUTION;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNRESOLVABLE_ELEMENT;
import static org.jboss.weld.logging.messages.BeanManagerMessage.UNRESOLVABLE_TYPE;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Qualifier;

import org.jboss.interceptor.registry.InterceptorRegistry;
import org.jboss.weld.Container;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.AbstractProcessInjectionTarget;
import org.jboss.weld.context.ContextNotActiveException;
import org.jboss.weld.context.CreationalContextImpl;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.el.Namespace;
import org.jboss.weld.el.WeldELResolver;
import org.jboss.weld.el.WeldExpressionFactory;
import org.jboss.weld.exceptions.AmbiguousResolutionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.ForbiddenArgumentException;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.exceptions.InjectionException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.ScopeModel;
import org.jboss.weld.resolution.NameBasedResolver;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableFactory;
import org.jboss.weld.resolution.ResolvableWeldClass;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.resolution.TypeSafeDecoratorResolver;
import org.jboss.weld.resolution.TypeSafeInterceptorResolver;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resolution.TypeSafeResolver;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.CopyOnWriteArrayListSupplier;
import org.jboss.weld.util.collections.IterableToIteratorFunction;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * Implementation of the Bean Manager.
 * 
 * Essentially a singleton for registering Beans, Contexts, Observers,
 * Interceptors etc. as well as providing resolution
 * 
 * @author Pete Muir
 * @author Marius Bogoevici
 */
public class BeanManagerImpl implements WeldManager, Serializable
{

   private static final long serialVersionUID = 3021562879133838561L;
   
   /*
    * Application scoped services 
    * ***************************
    */
   private transient final ServiceRegistry services;

   /*
    * Application scoped data structures 
    * ***********************************
    */
   
   // Contexts are shared across the application
   private transient final ListMultimap<Class<? extends Annotation>, Context> contexts;
   
   // Client proxies can be used application wide
   private transient final ClientProxyProvider clientProxyProvider;
   
   // TODO review this structure
   private transient final Map<EjbDescriptor<?>, SessionBean<?>> enterpriseBeans;
   
   // TODO This isn't right, specialization should follow accessibility rules, but I think we can enforce these in resolve()
   private transient final Map<Contextual<?>, Contextual<?>> specializedBeans;
   
   /*
    * Archive scoped data structures
    * ******************************
    */
   
   /* These data structures are all non-transitive in terms of bean deployment 
    * archive accessibility, and the configuration for this bean deployment
    * archive
    */
   private transient Collection<Class<?>> enabledAlternativeClasses;
   private transient Collection<Class<? extends Annotation>> enabledAlternativeStereotypes;
   private transient List<Class<?>> enabledDecoratorClasses;
   private transient List<Class<?>> enabledInterceptorClasses;
   private transient final Set<CurrentActivity> currentActivities;   

   /*
    * Activity scoped services 
    * *************************
    */ 
   
   /* These services are scoped to this activity only, but use data 
    * structures that are transitive accessible from other bean deployment 
    * archives
    */
   private transient final TypeSafeBeanResolver<Bean<?>> beanResolver;
   private transient final TypeSafeResolver<? extends Resolvable, Decorator<?>> decoratorResolver;
   private transient final TypeSafeResolver<? extends Resolvable, Interceptor<?>> interceptorResolver;
   private transient final TypeSafeResolver<? extends Resolvable, ObserverMethod<?>> observerResolver;
   private transient final NameBasedResolver nameBasedResolver;
   private transient final ELResolver weldELResolver;
   private transient Namespace rootNamespace;

   /*
    * Activity scoped data structures 
    * ********************************
    */
    
   /* These data structures are scoped to this bean deployment archive activity
    * only and represent the beans, decorators, interceptors, namespaces and 
    * observers deployed in this bean deployment archive activity
    */
   private transient final List<Bean<?>> beans;
   private transient final List<Bean<?>> transitiveBeans;
   private transient final List<Decorator<?>> decorators;
   private transient final List<Interceptor<?>> interceptors;
   private transient final List<String> namespaces;
   private transient final List<ObserverMethod<?>> observers;
   
   /*
    * These data structures represent the managers *accessible* from this bean 
    * deployment archive activity
    */
   private transient final HashSet<BeanManagerImpl> accessibleManagers;
   
   /*
    * This data structures represents child activities for this activity, it is
    * not transitively accessible
    */
   private transient final Set<BeanManagerImpl> childActivities;
   
   private final AtomicInteger childIds;
   private final String id;
   
   /*
    * Runtime data transfer
    * *********************
    */
   private transient final ThreadLocal<Stack<InjectionPoint>> currentInjectionPoint;

   /**
    * Interception model
    */
   private transient final InterceptorRegistry<Class<?>, SerializableContextual<Interceptor<?>, ?>> boundInterceptorsRegistry = new InterceptorRegistry<Class<?>, SerializableContextual<Interceptor<?>,?>>();
   private transient final InterceptorRegistry<Class<?>, Class<?>> declaredInterceptorsRegistry = new InterceptorRegistry<Class<?>, Class<?>>();

   /**
    * Create a new, root, manager
    * 
    * @param serviceRegistry
    * @return
    */
   public static BeanManagerImpl newRootManager(String id, ServiceRegistry serviceRegistry)
   {  
      ListMultimap<Class<? extends Annotation>, Context> contexts = Multimaps.newListMultimap(new ConcurrentHashMap<Class<? extends Annotation>, Collection<Context>>(), CopyOnWriteArrayListSupplier.<Context>instance());

      return new BeanManagerImpl(
            serviceRegistry, 
            new CopyOnWriteArrayList<Bean<?>>(),
            new CopyOnWriteArrayList<Bean<?>>(),
            new CopyOnWriteArrayList<Decorator<?>>(),
            new CopyOnWriteArrayList<Interceptor<?>>(),
            new CopyOnWriteArrayList<ObserverMethod<?>>(),
            new CopyOnWriteArrayList<String>(),
            new ConcurrentHashMap<EjbDescriptor<?>, SessionBean<?>>(),
            new ClientProxyProvider(),
            contexts, 
            new CopyOnWriteArraySet<CurrentActivity>(), 
            new HashMap<Contextual<?>, Contextual<?>>(), 
            new ArrayList<Class<?>>(),
            new ArrayList<Class<? extends Annotation>>(),
            new ArrayList<Class<?>>(),
            new ArrayList<Class<?>>(), 
            id,
            new AtomicInteger());
   }
   
   /**
    * Create a new, root, manager
    * 
    * @param serviceRegistry
    * @return
    */
   public static BeanManagerImpl newManager(BeanManagerImpl rootManager, String id, ServiceRegistry services)
   {  
      return new BeanManagerImpl(
            services, 
            new CopyOnWriteArrayList<Bean<?>>(),
            new CopyOnWriteArrayList<Bean<?>>(),
            new CopyOnWriteArrayList<Decorator<?>>(),
            new CopyOnWriteArrayList<Interceptor<?>>(),
            new CopyOnWriteArrayList<ObserverMethod<?>>(),
            new CopyOnWriteArrayList<String>(),
            rootManager.getEnterpriseBeans(),
            rootManager.getClientProxyProvider(),
            rootManager.getContexts(), 
            new CopyOnWriteArraySet<CurrentActivity>(), 
            new HashMap<Contextual<?>, Contextual<?>>(), 
            new ArrayList<Class<?>>(),
            new ArrayList<Class<? extends Annotation>>(),
            new ArrayList<Class<?>>(),
            new ArrayList<Class<?>>(),
            id,
            new AtomicInteger());
   }

   /**
    * Create a new child manager
    * 
    * @param parentManager
    * @return
    */
   public static BeanManagerImpl newChildActivityManager(BeanManagerImpl parentManager)
   {
      List<Bean<?>> beans = new CopyOnWriteArrayList<Bean<?>>();
      beans.addAll(parentManager.getBeans());
      List<Bean<?>> transitiveBeans = new CopyOnWriteArrayList<Bean<?>>();
      beans.addAll(parentManager.getTransitiveBeans());
      
      List<ObserverMethod<?>> registeredObservers = new CopyOnWriteArrayList<ObserverMethod<?>>();
      registeredObservers.addAll(parentManager.getObservers());
      List<String> namespaces = new CopyOnWriteArrayList<String>();
      namespaces.addAll(parentManager.getNamespaces());

      return new BeanManagerImpl(
            parentManager.getServices(), 
            beans, 
            transitiveBeans,
            parentManager.getDecorators(),
            parentManager.getInterceptors(),
            registeredObservers, 
            namespaces, 
            parentManager.getEnterpriseBeans(),  
            parentManager.getClientProxyProvider(), 
            parentManager.getContexts(), 
            parentManager.getCurrentActivities(), 
            parentManager.getSpecializedBeans(),
            parentManager.getEnabledAlternativeClasses(),
            parentManager.getEnabledAlternativeStereotypes(),
            parentManager.getEnabledDecoratorClasses(),
            parentManager.getEnabledInterceptorClasses(),
            new StringBuilder().append(parentManager.getChildIds().incrementAndGet()).toString(),
            parentManager.getChildIds());
   }

   /**
    * Create a new manager
    * @param enabledDecoratorClasses 
    * 
    * @param ejbServices the ejbResolver to use
    */
   private BeanManagerImpl(
         ServiceRegistry serviceRegistry, 
         List<Bean<?>> beans, 
         List<Bean<?>> transitiveBeans,
         List<Decorator<?>> decorators,
         List<Interceptor<?>> interceptors,
         List<ObserverMethod<?>> observers, 
         List<String> namespaces,
         Map<EjbDescriptor<?>, SessionBean<?>> enterpriseBeans, 
         ClientProxyProvider clientProxyProvider, 
         ListMultimap<Class<? extends Annotation>, Context> contexts, 
         Set<CurrentActivity> currentActivities, 
         Map<Contextual<?>, Contextual<?>> specializedBeans, 
         Collection<Class<?>> enabledAlternativeClasses,
         Collection<Class<? extends Annotation>> enabledAlternativeStereotypes,
         List<Class<?>> enabledDecoratorClasses,
         List<Class<?>> enabledInterceptorClasses,
         String id,
         AtomicInteger childIds)
   {
      this.services = serviceRegistry;
      this.beans = beans;
      this.transitiveBeans = transitiveBeans;
      this.decorators = decorators;
      this.interceptors = interceptors;
      this.enterpriseBeans = enterpriseBeans;
      this.clientProxyProvider = clientProxyProvider;
      this.contexts = contexts;
      this.currentActivities = currentActivities;
      this.specializedBeans = specializedBeans;
      this.observers = observers;
      this.enabledAlternativeClasses = enabledAlternativeClasses;
      this.enabledAlternativeStereotypes = enabledAlternativeStereotypes;
      setEnabledDecoratorClasses(enabledDecoratorClasses);
      setEnabledInterceptorClasses(enabledInterceptorClasses);
      this.namespaces = namespaces;
      this.id = id;
      this.childIds = new AtomicInteger();
      
      // Set up the structure to store accessible managers in
      this.accessibleManagers = new HashSet<BeanManagerImpl>();
      
      

      // TODO Currently we build the accessible bean list on the fly, we need to set it in stone once bootstrap is finished...
      Transform<Bean<?>> beanTransform = new BeanTransform(this);
      this.beanResolver = new TypeSafeBeanResolver<Bean<?>>(this, createDynamicAccessibleIterable(beanTransform));
      this.decoratorResolver = new TypeSafeDecoratorResolver(this, createDynamicAccessibleIterable(new DecoratorTransform()));
      this.interceptorResolver = new TypeSafeInterceptorResolver(this, createDynamicAccessibleIterable(new InterceptorTransform()));
      this.observerResolver = new TypeSafeObserverResolver(this, createDynamicAccessibleIterable(new ObserverMethodTransform()));
      this.nameBasedResolver = new NameBasedResolver(this, createDynamicAccessibleIterable(beanTransform));
      this.weldELResolver = new WeldELResolver(this);
      this.childActivities = new CopyOnWriteArraySet<BeanManagerImpl>();
      
      this.currentInjectionPoint = new ThreadLocal<Stack<InjectionPoint>>()
      {
         @Override
         protected Stack<InjectionPoint> initialValue()
         {
            return new Stack<InjectionPoint>();
         }
      };
   }
   
   private <T> Set<Iterable<T>> buildAccessibleClosure(Collection<BeanManagerImpl> hierarchy, Transform<T> transform)
   {
      Set<Iterable<T>> result = new HashSet<Iterable<T>>();
      hierarchy.add(this);
      result.add(transform.transform(this));
      for (BeanManagerImpl beanManager : accessibleManagers)
      {
         // Only add if we aren't already in the tree (remove cycles)
         if (!hierarchy.contains(beanManager))
         {
            result.addAll(beanManager.buildAccessibleClosure(new ArrayList<BeanManagerImpl>(hierarchy), transform));
         }
      }
      return result;
   }
   
   private <T> Iterable<T> createDynamicAccessibleIterable(final Transform<T> transform)
   {
      return new Iterable<T>()
      {

         public Iterator<T> iterator()
         {
            Set<Iterable<T>> iterable = buildAccessibleClosure(new ArrayList<BeanManagerImpl>(), transform);
            return Iterators.concat(Iterators.transform(iterable.iterator(), IterableToIteratorFunction.<T>instance()));
         }
         
      };
   }
   
   private <T> Iterable<T> createStaticAccessibleIterable(final Transform<T> transform)
   {
      Set<Iterable<T>> iterable = buildAccessibleClosure(new ArrayList<BeanManagerImpl>(), transform);
      return Iterables.concat(iterable); 
   }
   
   public void addAccessibleBeanManager(BeanManagerImpl accessibleBeanManager)
   {
      accessibleManagers.add(accessibleBeanManager);
      beanResolver.clear();
   }

   public void addBean(Bean<?> bean)
   {
      if (beans.contains(bean))
      {
         return;
      }
      if (bean.getClass().equals(SessionBean.class))
      {
         SessionBean<?> enterpriseBean = (SessionBean<?>) bean;
         enterpriseBeans.put(enterpriseBean.getEjbDescriptor(), enterpriseBean);
      }
      if (bean instanceof PassivationCapable)
      {
         Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
      }
      registerBeanNamespace(bean);
      for (BeanManagerImpl childActivity : childActivities)
      {
         childActivity.addBean(bean);
      }
      // New beans and most built in beans aren't resolvable transtively
      if (bean instanceof ExtensionBean || (!(bean instanceof NewBean) && !(bean instanceof AbstractBuiltInBean<?>)))
      {
         this.transitiveBeans.add(bean);
      }
      this.beans.add(bean);
      beanResolver.clear();
   }
   
   public void addDecorator(Decorator<?> bean)
   {
      decorators.add(bean);
      getServices().get(ContextualStore.class).putIfAbsent(bean);
      decoratorResolver.clear();
   }
   
   public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings)
   {
      Observers.checkEventObjectType(event);
      return this.<T>resolveObserverMethods(event.getClass(), bindings);
   }

   public void addInterceptor(Interceptor<?> bean)
   {
      interceptors.add(bean);
      getServices().get(ContextualStore.class).putIfAbsent(bean);
      interceptorResolver.clear();
   }


   @SuppressWarnings("unchecked")
   public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Type eventType, Annotation... bindings)
   {
      checkBindingTypes(Arrays.asList(bindings));    
      HashSet<Annotation> bindingAnnotations = new HashSet<Annotation>(Arrays.asList(bindings));
      bindingAnnotations.add(AnyLiteral.INSTANCE);
      Set<ObserverMethod<? super T>> observers = new HashSet<ObserverMethod<? super T>>();
      Set<ObserverMethod<?>> eventObservers = observerResolver.resolve(ResolvableFactory.of(new HierarchyDiscovery(eventType).getTypeClosure(),  bindingAnnotations, null));
      for (ObserverMethod<?> observer : eventObservers)
      {
         observers.add((ObserverMethod<T>) observer);
      }
      return observers;
   }
   
   private void checkBindingTypes(Collection<Annotation> bindings)
   {
      HashSet<Annotation> bindingAnnotations = new HashSet<Annotation>(bindings);
      for (Annotation annotation : bindings)
      {
         if (!getServices().get(MetaAnnotationStore.class).getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new ForbiddenArgumentException(INVALID_QUALIFIER, annotation);
         }
      }
      if (bindingAnnotations.size() < bindings.size())
      {
         throw new ForbiddenArgumentException(DUPLICATE_QUALIFIERS, bindings);
      }

   }

   /**
    * A collection of enabled alternative classes
    * 
    */
   public Collection<Class<?>> getEnabledAlternativeClasses()
   {
      return Collections.unmodifiableCollection(enabledAlternativeClasses);
   }
   
   /**
    * @return the enabled alternative stereotypes
    */
   public Collection<Class<? extends Annotation>> getEnabledAlternativeStereotypes()
   {
      return Collections.unmodifiableCollection(enabledAlternativeStereotypes);
   }
   
   public boolean isBeanEnabled(Bean<?> bean)
   {
      return Beans.isBeanEnabled(bean, getEnabledAlternativeClasses(), getEnabledAlternativeStereotypes());   
   }

   /**
    * @return the enabledDecoratorClasses
    */
   public List<Class<?>> getEnabledDecoratorClasses()
   {
      return Collections.unmodifiableList(enabledDecoratorClasses);
   }
   
   /**
    * @return the enabledInterceptorClasses
    */
   public List<Class<?>> getEnabledInterceptorClasses()
   {
      return Collections.unmodifiableList(enabledInterceptorClasses);
   }

   public void setEnabledAlternativeClasses(Collection<Class<?>> enabledAlternativeClasses)
   {
      this.enabledAlternativeClasses = enabledAlternativeClasses;
   }
   
   public void setEnabledAlternativeStereotypes(Collection<Class<? extends Annotation>> enabledAlternativeSterotypes)
   {
      this.enabledAlternativeStereotypes = enabledAlternativeSterotypes;
   }
   
   public void setEnabledDecoratorClasses(List<Class<?>> enabledDecoratorClasses)
   {
      this.enabledDecoratorClasses = enabledDecoratorClasses;
   }
   
   public void setEnabledInterceptorClasses(List<Class<?>> enabledInterceptorClasses)
   {
      this.enabledInterceptorClasses = enabledInterceptorClasses;
   }
   
   public Set<Bean<?>> getBeans(Type beanType, Annotation... bindings)
   {
      return getBeans(ResolvableWeldClass.of(beanType, bindings, this), bindings);
   }
   
   public Set<Bean<?>> getBeans(WeldAnnotated<?, ?> element, Annotation... bindings)
   {
      for (Annotation annotation : element.getAnnotations())
      {
         if (!getServices().get(MetaAnnotationStore.class).getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new ForbiddenArgumentException(INVALID_QUALIFIER, annotation);
         }
      }
//      for (Type type : element.getActualTypeArguments())
//      {
//         if (type instanceof WildcardType)
//         {
//            throw new IllegalArgumentException("Cannot resolve a type parameterized with a wildcard " + element);
//         }
//         if (type instanceof TypeVariable<?>)
//         {
//            throw new IllegalArgumentException("Cannot resolve a type parameterized with a type parameter " + element);
//         }
//      }
      if (bindings != null && bindings.length > element.getMetaAnnotations(Qualifier.class).size())
      {
         throw new ForbiddenArgumentException(DUPLICATE_QUALIFIERS, Arrays.asList(bindings));
      }
      return beanResolver.resolve(ResolvableFactory.of(element));
   }

   public Set<Bean<?>> getInjectableBeans(InjectionPoint injectionPoint)
   {
      boolean registerInjectionPoint = !injectionPoint.getType().equals(InjectionPoint.class);
      try
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().push(injectionPoint);
         }
         // TODO Do this properly
         Set<Bean<?>> beans = getBeans(ResolvableWeldClass.of(injectionPoint.getType(), injectionPoint.getQualifiers().toArray(new Annotation[0]), this));
         Set<Bean<?>> injectableBeans = new HashSet<Bean<?>>();
         for (Bean<?> bean : beans)
         {
            if (!(bean instanceof Decorator || bean instanceof Interceptor))
            {
               injectableBeans.add(bean);
            }
         }
         return injectableBeans;
      }
      finally
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().pop();
         }
      }
   }
   
   protected void registerBeanNamespace(Bean<?> bean)
   {
      if (bean.getName() != null && bean.getName().indexOf('.') > 0)
      {
         namespaces.add(bean.getName().substring(0, bean.getName().lastIndexOf('.')));
      }
   }

   /**
    * Gets the class-mapped beans. For internal use.
    * 
    * @return The bean map
    */
   public Map<EjbDescriptor<?>, SessionBean<?>> getEnterpriseBeans()
   {
      return enterpriseBeans;
   }

   /**
    * The beans registered with the Web Bean manager which are resolvable. Does
    * not include interceptor and decorator beans
    * 
    * @return The list of known beans
    */
   public List<Bean<?>> getBeans()
   {
      return Collections.unmodifiableList(beans);
   }
   
   List<Bean<?>> getTransitiveBeans()
   {
      return Collections.unmodifiableList(transitiveBeans);
   }
   
   public List<Decorator<?>> getDecorators()
   {
      return Collections.unmodifiableList(decorators);
   }

    public List<Interceptor<?>> getInterceptors()
   {
      return Collections.unmodifiableList(interceptors);
   }
   
   public Iterable<Bean<?>> getAccessibleBeans()
   {
      return createDynamicAccessibleIterable(new BeanTransform(this));
   }

   public void addContext(Context context)
   {
      contexts.put(context.getScope(), context);
   }

   /**
    * Does the actual observer registration
    * 
    * @param observer
=    */
   public void addObserver(ObserverMethod<?> observer)
   {
      //checkEventType(observer.getObservedType());
      observers.add(observer);
      for (BeanManagerImpl childActivity : childActivities)
      {
         childActivity.addObserver(observer);
      }
   }
   
   /**
    * Fires an event object with given event object for given bindings
    * 
    * @param event The event object to pass along
    * @param qualifiers The binding types to match
    * 
    * @see javax.enterprise.inject.spi.BeanManager#fireEvent(java.lang.Object,
    *      java.lang.annotation.Annotation[])
    */
   public void fireEvent(Object event, Annotation... qualifiers)
   {
      fireEvent(event.getClass(), event, qualifiers);
   }
   
   public void fireEvent(Type eventType, Object event, Annotation... qualifiers)
   {
      Observers.checkEventObjectType(event);
      notifyObservers(event, resolveObserverMethods(eventType, qualifiers));
   }

   private <T> void notifyObservers(final T event, final Set<ObserverMethod<? super T>> observers)
   {
      for (ObserverMethod<? super T> observer : observers)
      {
         observer.notify(event);
      }     
   }

   /**
    * Gets an active context of the given scope. Throws an exception if there
    * are no active contexts found or if there are too many matches
    * 
    * @param scopeType The scope to match
    * @return A single active context of the given scope
    * 
    * @see javax.enterprise.inject.spi.BeanManager#getContext(java.lang.Class)
    */
   public Context getContext(Class<? extends Annotation> scopeType)
   {
      List<Context> activeContexts = new ArrayList<Context>();
      for (Context context : contexts.get(scopeType))
      {
         if (context.isActive())
         {
            activeContexts.add(context);
         }
      }
      if (activeContexts.isEmpty())
      {
         throw new ContextNotActiveException(CONTEXT_NOT_ACTIVE, scopeType.getName());
      }
      if (activeContexts.size() > 1)
      {
         throw new ForbiddenStateException(DUPLICATE_ACTIVE_CONTEXTS, scopeType.getName());
      }
      return activeContexts.iterator().next();

   }
   
   public Object getReference(Bean<?> bean, CreationalContext<?> creationalContext, boolean delegate)
   {
      bean = getMostSpecializedBean(bean);
      if (creationalContext instanceof WeldCreationalContext<?>)
      {
         creationalContext = ((WeldCreationalContext<?>) creationalContext).getCreationalContext(bean);
      }
      if (!delegate && isProxyRequired(bean))
      {
         if (creationalContext != null || getContext(bean.getScope()).get(bean) != null)
         {
            return clientProxyProvider.getClientProxy(this, bean);
         }
         else
         {
            return null;
         }
      }
      else
      {
         return getContext(bean.getScope()).get((Contextual) bean, creationalContext);
      }
   }
   
   private boolean isProxyRequired(Bean<?> bean)
   {
      if (getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal())
      {
         return true;
      }
      else if (bean instanceof RIBean<?>)
      {
         return ((RIBean<?>) bean).isProxyRequired();
      }
      else
      {
         return false;
      }
   }

   public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> creationalContext)
   {
      if (!Reflections.isAssignableFrom(bean.getTypes(), beanType))
      {
         throw new ForbiddenArgumentException(SPECIFIED_TYPE_NOT_BEAN_TYPE, beanType, bean );
      }
      return getReference(bean, creationalContext, false);
   }

   
   /**
    * Get a reference, registering the injection point used.
    * 
    * @param injectionPoint the injection point to register
    * @param resolvedBean the bean to get a reference to 
    * @param creationalContext the creationalContext
    * @return
    */
   public Object getReference(InjectionPoint injectionPoint, Bean<?> resolvedBean, CreationalContext<?> creationalContext)
   {
      boolean registerInjectionPoint = (injectionPoint != null && !injectionPoint.getType().equals(InjectionPoint.class));
      boolean delegateInjectionPoint = injectionPoint != null && injectionPoint.isDelegate();
      try
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().push(injectionPoint);
         }
         if (getServices().get(MetaAnnotationStore.class).getScopeModel(resolvedBean.getScope()).isNormal() && !Proxies.isTypeProxyable(injectionPoint.getType()))
         {
            throw new UnproxyableResolutionException(UNPROXYABLE_RESOLUTION, resolvedBean, injectionPoint);
         }
         // TODO Can we move this logic to getReference?
         if (creationalContext instanceof WeldCreationalContext<?>)
         {
            WeldCreationalContext<?> wbCreationalContext = (WeldCreationalContext<?>) creationalContext;
            if (wbCreationalContext.containsIncompleteInstance(resolvedBean))
            {
               return wbCreationalContext.getIncompleteInstance(resolvedBean);
            }
            else
            {
               return getReference(resolvedBean, wbCreationalContext, delegateInjectionPoint);
            }
         }
         else
         {
            return getReference(resolvedBean, creationalContext, delegateInjectionPoint);
         }
      }
      finally
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().pop();
         }
      }
   }
  

   public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> creationalContext)
   {
         WeldAnnotated<?, ?> element = ResolvableWeldClass.of(injectionPoint.getType(), injectionPoint.getQualifiers().toArray(new Annotation[0]), this);
         Bean<?> resolvedBean = getBean(element, element.getBindingsAsArray());
         return getReference(injectionPoint, resolvedBean, creationalContext);
   }

   /**
    * Returns an instance by API type and binding types
    * 
    * @param beanType The API type to match
    * @param bindings The binding types to match
    * @return An instance of the bean
    * 
    */
   @Deprecated
   public <T> T getInstanceByType(Class<T> beanType, Annotation... bindings)
   {
      Set<Bean<?>> beans = getBeans(beanType, bindings);
      Bean<?> bean = resolve(beans);
      if (bean == null)
      {
         throw new UnsatisfiedResolutionException(UNRESOLVABLE_TYPE, beanType, Arrays.toString(bindings)); 
      }
      Object reference = getReference(bean, beanType, createCreationalContext(bean));
      
      @SuppressWarnings("unchecked")
      T instance = (T) reference;
      
      return instance;
   }

   public <T> Bean<T> getBean(WeldAnnotated<T, ?> element, Annotation... bindings)
   {
      Bean<T> bean = (Bean<T>) resolve(getBeans(element, bindings));
      if (bean == null)
      {
         throw new UnsatisfiedResolutionException(UNRESOLVABLE_ELEMENT, element);
      }
      
      boolean normalScoped = getServices().get(MetaAnnotationStore.class).getScopeModel(bean.getScope()).isNormal();
      if (normalScoped && !Beans.isBeanProxyable(bean))
      {
         throw new UnproxyableResolutionException(NOT_PROXYABLE, bean);
      }
      return bean;
   }

   public Set<Bean<?>> getBeans(String name)
   {
      return nameBasedResolver.resolve(name);
   }

   /**
    * Resolves a list of decorators based on API types and binding types
    * 
    * @param types The set of API types to match
    * @param bindings The binding types to match
    * @return A list of matching decorators
    * 
    * @see javax.enterprise.inject.spi.BeanManager#resolveDecorators(java.util.Set,
    *      java.lang.annotation.Annotation[])
    */
   public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... bindings)
   {
      checkResolveDecoratorsArguments(types, Arrays.asList(bindings));
      // TODO Fix this cast and make the resolver return a list
      return new ArrayList<Decorator<?>>(decoratorResolver.resolve(ResolvableFactory.of(types, null, bindings)));
   }
   
   public List<Decorator<?>> resolveDecorators(Set<Type> types, Set<Annotation> bindings)
   {
      checkResolveDecoratorsArguments(types, bindings);
      // TODO Fix this cast and make the resolver return a list
      return new ArrayList<Decorator<?>>(decoratorResolver.resolve(ResolvableFactory.of(types, bindings, null)));
   }

   private void checkResolveDecoratorsArguments(Set<Type> types, Collection<Annotation> bindings)
   {
      if (types.isEmpty())
      {
         throw new ForbiddenArgumentException(NO_DECORATOR_TYPES);
      }
      checkBindingTypes(bindings);
   }

   /**
    * Resolves a list of interceptors based on interception type and interceptor
    * bindings
    * 
    * @param type The interception type to resolve
    * @param interceptorBindings The binding types to match
    * @return A list of matching interceptors
    * 
    * @see javax.enterprise.inject.spi.BeanManager#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType,
    *      java.lang.annotation.Annotation[])
    */
   public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
   {
      if (interceptorBindings.length == 0)
         throw new ForbiddenArgumentException(INTERCEPTOR_BINDINGS_EMPTY);
      Set<Class<?>> uniqueInterceptorBindings = new HashSet<Class<?>>();
      for (Annotation interceptorBinding: interceptorBindings)
      {
         if (uniqueInterceptorBindings.contains(interceptorBinding.annotationType()))
            throw new ForbiddenArgumentException(DUPLICATE_INTERCEPTOR_BINDING, interceptorBinding.annotationType());
         if (!isInterceptorBinding(interceptorBinding.annotationType()))
            throw new ForbiddenArgumentException(INTERCEPTOR_RESOLUTION_WITH_NONBINDING_TYPE, interceptorBinding.annotationType());
         uniqueInterceptorBindings.add(interceptorBinding.annotationType());
      }
      return new ArrayList<Interceptor<?>>(interceptorResolver.resolve(ResolvableFactory.of(type,interceptorBindings)));
   }

   /**
    * Get the web bean resolver. For internal use
    * 
    * @return The resolver
    */
   public TypeSafeBeanResolver<Bean<?>> getBeanResolver()
   {
      return beanResolver;
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Manager\n");
      buffer.append("Enabled alternatives: " + getEnabledAlternativeClasses() + " " + getEnabledAlternativeStereotypes() + "\n");
      buffer.append("Registered contexts: " + contexts.keySet() + "\n");
      buffer.append("Registered beans: " + getBeans().size() + "\n");
      buffer.append("Specialized beans: " + specializedBeans.size() + "\n");
      return buffer.toString();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof BeanManagerImpl)
      {
         BeanManagerImpl that = (BeanManagerImpl) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

   public BeanManagerImpl createActivity()
   {
      BeanManagerImpl childActivity = newChildActivityManager(this);
      childActivities.add(childActivity);
      Container.instance().addActivity(childActivity);
      return childActivity;
   }

   public BeanManagerImpl setCurrent(Class<? extends Annotation> scopeType)
   {
      if (!getServices().get(MetaAnnotationStore.class).getScopeModel(scopeType).isNormal())
      {
         throw new ForbiddenArgumentException(NON_NORMAL_SCOPE, scopeType);
      }
      currentActivities.add(new CurrentActivity(getContext(scopeType), this));
      return this;
   }
   
   public BeanManagerImpl getCurrent()
   {
      List<CurrentActivity> activeCurrentActivities = new ArrayList<CurrentActivity>();
      for (CurrentActivity currentActivity : currentActivities)
      {
         if (currentActivity.getContext().isActive())
         {
            activeCurrentActivities.add(currentActivity);
         }
      }
      if (activeCurrentActivities.size() == 0)
      {
         return this;
      }
      else if (activeCurrentActivities.size() == 1)
      {
         return activeCurrentActivities.get(0).getManager();
      }
      throw new ForbiddenStateException(TOO_MANY_ACTIVITIES, currentActivities);
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

   /**
    * The injection point being operated on for this thread
    * 
    * @return the current injection point
    */
   public InjectionPoint getCurrentInjectionPoint()
   {
      if (!currentInjectionPoint.get().empty())
      {
         return currentInjectionPoint.get().peek();
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Replaces (or adds) the current injection point. If a current injection 
    * point exists, it will be replaced. If no current injection point exists, 
    * one will be added.
    * 
    * @param injectionPoint the injection point to use
    * @return the injection point added, or null if non previous existed
    */
   public InjectionPoint replaceOrPushCurrentInjectionPoint(InjectionPoint injectionPoint)
   {
      InjectionPoint originalInjectionPoint = null;
      if (!currentInjectionPoint.get().empty())
      {
         originalInjectionPoint = currentInjectionPoint.get().pop();
      }
      currentInjectionPoint.get().push(injectionPoint);
      return originalInjectionPoint;
   }
   
   public void pushDummyInjectionPoint()
   {
      currentInjectionPoint.get().push(DummyInjectionPoint.INSTANCE);
   }
   
   public void popDummyInjectionPoint()
   {
      if (!currentInjectionPoint.get().isEmpty() && DummyInjectionPoint.INSTANCE.equals(currentInjectionPoint.get().peek()))
      {
         currentInjectionPoint.get().pop();
      }
   }

   /**
    * 
    * @return
    */
   public Map<Contextual<?>, Contextual<?>> getSpecializedBeans()
   {
      // TODO make this unmodifiable after deploy!
      return specializedBeans;
   }

   // Serialization

   protected Object readResolve()
   {
      return Container.instance().activityManager(id);
   }
   
   protected ClientProxyProvider getClientProxyProvider()
   {
      return clientProxyProvider;
   }
   
   protected ListMultimap<Class<? extends Annotation>, Context> getContexts()
   {
      return contexts;
   }
   
   /**
    * @return the namespaces
    */
   protected List<String> getNamespaces()
   {
      return namespaces;
   }
   
   public Iterable<String> getAccessibleNamespaces()
   {
      // TODO Cache this
      return createDynamicAccessibleIterable(new NamespaceTransform());
   }
   
   private Set<CurrentActivity> getCurrentActivities()
   {
      return currentActivities;
   }
   
   public String getId()
   {
      return id;
   }
   
   public AtomicInteger getChildIds()
   {
      return childIds;
   }
   
   public List<ObserverMethod<?>> getObservers()
   {
      return observers;
   }
   
   public Namespace getRootNamespace()
   {
      // TODO I don't like this lazy init
      if (rootNamespace == null)
      {
         rootNamespace = new Namespace(createDynamicAccessibleIterable(new NamespaceTransform()));
      }
      return rootNamespace;
   }

   public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type)
   {
      return new SimpleInjectionTarget<T>(getServices().get(ClassTransformer.class).loadClass(type), this);
   }
   
   public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor)
   {
      return getBean(descriptor).getInjectionTarget();
   }

   public <X> Bean<? extends X> getMostSpecializedBean(Bean<X> bean)
   {
      Contextual<?> key = bean;
      while (specializedBeans.containsKey(key))
      {
         if (key == null)
         {
            System.out.println("null key " + bean);
         }
         key = specializedBeans.get(key);
      }
      return (Bean<X>) key;
   }

   public void validate(InjectionPoint ij)
   {
      try
      {
         getServices().get(Validator.class).validateInjectionPoint(ij, this);
      }
      catch (DeploymentException e) 
      {
         throw new InjectionException(e.getLocalizedMessage(), e.getCause());
      }
   }

   public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType)
   {
      if (getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(bindingType).isValid())
      {
         return getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(bindingType).getMetaAnnotations();
      }
      else
      {
         throw new ForbiddenArgumentException(NOT_INTERCEPTOR_BINDING_TYPE, bindingType);
      }
   }

   public Bean<?> getPassivationCapableBean(String id)
   {
      return getServices().get(ContextualStore.class).<Bean<Object>, Object>getContextual(id);
   }

   public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype)
   {
      if (getServices().get(MetaAnnotationStore.class).getStereotype(stereotype).isValid())
      {
         return getServices().get(MetaAnnotationStore.class).getStereotype(stereotype).getMetaAnnotations();
      }
      else
      {
         throw new ForbiddenArgumentException(NOT_STEREOTYPE, stereotype);
      }
   }

   public boolean isQualifier(Class<? extends Annotation> annotationType)
   {
      return getServices().get(MetaAnnotationStore.class).getBindingTypeModel(annotationType).isValid();
   }

   public boolean isInterceptorBinding(Class<? extends Annotation> annotationType)
   {
      return getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotationType).isValid();
   }

   public boolean isNormalScope(Class<? extends Annotation> annotationType)
   {
      ScopeModel<?> scope = getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType);
      return scope.isValid() && scope.isNormal(); 
   }
   
   public boolean isPassivatingScope(Class<? extends Annotation> annotationType)
   {
      ScopeModel<?> scope = getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType);
      return scope.isValid() && scope.isPassivating();
   }
   
   public boolean isScope(Class<? extends Annotation> annotationType)
   {
      return getServices().get(MetaAnnotationStore.class).getScopeModel(annotationType).isValid();
   }

   public boolean isStereotype(Class<? extends Annotation> annotationType)
   {
      return getServices().get(MetaAnnotationStore.class).getStereotype(annotationType).isValid();
   }

   public ELResolver getELResolver()
   {
      return weldELResolver;
   }
   
   public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory)
   {
      return new WeldExpressionFactory(expressionFactory);
   }
   
   public <T> WeldCreationalContext<T> createCreationalContext(Contextual<T> contextual)
   {
      return new CreationalContextImpl<T>(contextual);
   }

   public <T> AnnotatedType<T> createAnnotatedType(Class<T> type)
   {
      return getServices().get(ClassTransformer.class).loadClass(type);
   }

   public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans)
   {
      Set<Bean<? extends X>> resolvedBeans = beanResolver.resolve(beans);
      if (resolvedBeans.size() == 0)
      {
         return null;
      }
      if (resolvedBeans.size() == 1)
      {
         return resolvedBeans.iterator().next();
      }
      else
      {
         throw new AmbiguousResolutionException(AMBIGUOUS_BEANS_FOR_DEPENDENCY, beans);
      }
   }

   public <T> EjbDescriptor<T> getEjbDescriptor(String beanName)
   {
      return getServices().get(EjbDescriptors.class).get(beanName);
   }
   
   public <T> SessionBean<T> getBean(EjbDescriptor<T> descriptor)
   {
      return (SessionBean<T>) getEnterpriseBeans().get(descriptor);
   }
   
   public void cleanup()
   {
      services.cleanup();
      this.currentInjectionPoint.remove();
      this.accessibleManagers.clear();
      this.beanResolver.clear();
      this.beans.clear();
      this.childActivities.clear();
      this.clientProxyProvider.clear();
      this.contexts.clear();
      this.currentActivities.clear();
      this.decoratorResolver.clear();
      this.decorators.clear();
      this.enabledDecoratorClasses.clear();
      this.enabledInterceptorClasses.clear();
      this.enabledAlternativeClasses.clear();
      this.enabledAlternativeStereotypes.clear();
      this.enterpriseBeans.clear();
      this.interceptorResolver.clear();
      this.interceptors.clear();
      this.nameBasedResolver.clear();
      this.namespaces.clear();
      this.observerResolver.clear();
      this.observers.clear();
      this.specializedBeans.clear();
   }

   public InterceptorRegistry<Class<?>, SerializableContextual<Interceptor<?>, ?>> getCdiInterceptorsRegistry()
   {
      return boundInterceptorsRegistry;
   }

   public InterceptorRegistry<Class<?>, Class<?>> getClassDeclaredInterceptorsRegistry()
   {
      return declaredInterceptorsRegistry;
   }
   
   public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType)
   {
      return AbstractProcessInjectionTarget.fire(this, annotatedType, createInjectionTarget(annotatedType));
   }
}
