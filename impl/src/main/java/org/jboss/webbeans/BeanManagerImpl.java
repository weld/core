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
package org.jboss.webbeans;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.el.ELResolver;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ScopeType;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.deployment.Production;
import javax.enterprise.inject.deployment.Standard;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ManagedBean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.event.Observer;
import javax.inject.DeploymentException;
import javax.inject.DuplicateBindingTypeException;

import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.proxy.ClientProxyProvider;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.CreationalContextImpl;
import org.jboss.webbeans.el.Namespace;
import org.jboss.webbeans.el.WebBeansELResolverImpl;
import org.jboss.webbeans.event.EventManager;
import org.jboss.webbeans.event.EventObserver;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.NonContextualInjector;
import org.jboss.webbeans.injection.resolution.DecoratorResolver;
import org.jboss.webbeans.injection.resolution.ResolvableFactory;
import org.jboss.webbeans.injection.resolution.ResolvableWBClass;
import org.jboss.webbeans.injection.resolution.Resolver;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.manager.api.WebBeansManager;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Proxies;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.collections.multi.ConcurrentListHashMultiMap;
import org.jboss.webbeans.util.collections.multi.ConcurrentListMultiMap;
import org.jboss.webbeans.util.collections.multi.ConcurrentSetHashMultiMap;
import org.jboss.webbeans.util.collections.multi.ConcurrentSetMultiMap;

/**
 * Implementation of the Web Beans Manager.
 * 
 * Essentially a singleton for registering Beans, Contexts, Observers,
 * Interceptors etc. as well as providing resolution
 * 
 * @author Pete Muir
 * 
 */
public class BeanManagerImpl implements WebBeansManager, Serializable
{
   
   private static class CurrentActivity
   {
	
      private final Context context;
      private final BeanManagerImpl manager;
		
      public CurrentActivity(Context context, BeanManagerImpl manager)
      {
         this.context = context;
         this.manager = manager;
      }

      public Context getContext()
      {
         return context;
      }
      
      public BeanManagerImpl getManager()
      {
         return manager;
      }
      
      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof CurrentActivity)
         {
            return this.getContext().equals(((CurrentActivity) obj).getContext());
         }
         else
         {
            return false;
         }
      }
      
      @Override
      public int hashCode()
      {
         return getContext().hashCode();
      }
      
      @Override
      public String toString()
      {
         return getContext() + " -> " + getManager();
      }
   }
   
   private static final Log log = Logging.getLog(BeanManagerImpl.class);

   private static final long serialVersionUID = 3021562879133838561L;

   // The JNDI key to place the manager under
   public static final String JNDI_KEY = "java:app/Manager";
   
   /*
    * Application scoped services 
    * ***************************
    */
   private transient final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
   private transient final ServiceRegistry services;

   /*
    * Application scoped data structures 
    * ***********************************
    */
   private transient List<Class<? extends Annotation>> enabledDeploymentTypes;
   private transient List<Class<?>> enabledDecoratorClasses;
   private transient List<Class<?>> enabledInterceptorClasses;
   private transient final ConcurrentListMultiMap<Class<? extends Annotation>, Context> contexts;
   private final transient Set<CurrentActivity> currentActivities;
   private transient final ClientProxyProvider clientProxyProvider;
   private transient final Map<Class<?>, EnterpriseBean<?>> newEnterpriseBeans;
   private transient final Map<String, RIBean<?>> riBeans;
   private final transient Map<Bean<?>, Bean<?>> specializedBeans;
   private final transient AtomicInteger ids;

   /*
    * Activity scoped services 
    * *************************
    */
   private transient final EventManager eventManager;
   private transient final Resolver resolver;
   private transient final Resolver decoratorResolver;
   private final transient NonContextualInjector nonContextualInjector;
   private final transient ELResolver webbeansELResolver;

   /*
    * Activity scoped data structures 
    * ********************************
    */
   private transient final ThreadLocal<Stack<InjectionPoint>> currentInjectionPoint;
   private transient final List<Bean<?>> beans;
   private transient final List<DecoratorBean<?>> decorators;
   private final transient Namespace rootNamespace;
   private final transient ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers;
   private final transient Set<BeanManagerImpl> childActivities;
   private final Integer id;

   /**
    * Create a new, root, manager
    * 
    * @param serviceRegistry
    * @return
    */
   public static BeanManagerImpl newRootManager(ServiceRegistry serviceRegistry)
   {
      List<Class<? extends Annotation>> defaultEnabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      defaultEnabledDeploymentTypes.add(0, Standard.class);
      defaultEnabledDeploymentTypes.add(1, Production.class);
      
      List<Class<?>> defaultEnabledDecoratorClasses = new ArrayList<Class<?>>();

      return new BeanManagerImpl(
            serviceRegistry, 
            new CopyOnWriteArrayList<Bean<?>>(),
            new CopyOnWriteArrayList<DecoratorBean<?>>(),
            new ConcurrentSetHashMultiMap<Type, EventObserver<?>>(),
            new Namespace(),
            new ConcurrentHashMap<Class<?>, EnterpriseBean<?>>(),
            new ConcurrentHashMap<String, RIBean<?>>(),
            new ClientProxyProvider(),
            new ConcurrentListHashMultiMap<Class<? extends Annotation>, Context>(),
            new CopyOnWriteArraySet<CurrentActivity>(), 
            new HashMap<Bean<?>, Bean<?>>(), defaultEnabledDeploymentTypes, defaultEnabledDecoratorClasses, 
            new AtomicInteger());
   }

   /**
    * Create a new child manager
    * 
    * @param parentManager
    * @return
    */
   public static BeanManagerImpl newChildManager(BeanManagerImpl parentManager)
   {
      List<Bean<?>> beans = new CopyOnWriteArrayList<Bean<?>>();
      beans.addAll(parentManager.getBeans());
      
      ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers = new ConcurrentSetHashMultiMap<Type, EventObserver<?>>();
      registeredObservers.deepPutAll(parentManager.getRegisteredObservers());
      Namespace rootNamespace = new Namespace(parentManager.getRootNamespace());

      return new BeanManagerImpl(
            parentManager.getServices(), 
            beans, parentManager.getDecorators(), 
            registeredObservers, rootNamespace, 
            parentManager.getNewEnterpriseBeanMap(), 
            parentManager.getRiBeans(), 
            parentManager.getClientProxyProvider(), 
            parentManager.getContexts(), 
            parentManager.getCurrentActivities(), 
            parentManager.getSpecializedBeans(), 
            parentManager.getEnabledDeploymentTypes(), 
            parentManager.getEnabledDecoratorClasses(), 
            parentManager.getIds());
   }

   /**
    * Create a new manager
    * @param enabledDecoratorClasses 
    * 
    * @param ejbServices the ejbResolver to use
    */
   private BeanManagerImpl(ServiceRegistry serviceRegistry, List<Bean<?>> beans, List<DecoratorBean<?>> decorators, ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers, Namespace rootNamespace, Map<Class<?>, EnterpriseBean<?>> newEnterpriseBeans, Map<String, RIBean<?>> riBeans, ClientProxyProvider clientProxyProvider, ConcurrentListMultiMap<Class<? extends Annotation>, Context> contexts, Set<CurrentActivity> currentActivities, Map<Bean<?>, Bean<?>> specializedBeans, List<Class<? extends Annotation>> enabledDeploymentTypes, List<Class<?>> enabledDecoratorClasses, AtomicInteger ids)
   {
      this.services = serviceRegistry;
      this.beans = beans;
      this.decorators = decorators;
      this.newEnterpriseBeans = newEnterpriseBeans;
      this.riBeans = riBeans;
      this.clientProxyProvider = clientProxyProvider;
      this.contexts = contexts;
      this.currentActivities = currentActivities;
      this.specializedBeans = specializedBeans;
      this.registeredObservers = registeredObservers;
      setEnabledDeploymentTypes(enabledDeploymentTypes);
      setEnabledDecoratorClasses(enabledDecoratorClasses);
      this.rootNamespace = rootNamespace;
      this.ids = ids;
      this.id = ids.incrementAndGet();

      this.resolver = new Resolver(this, beans);
      this.decoratorResolver = new DecoratorResolver(this, decorators);
      this.eventManager = new EventManager(this);
      this.nonContextualInjector = new NonContextualInjector(this);
      this.webbeansELResolver = new WebBeansELResolverImpl(this);
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

   /**
    * Set up the enabled deployment types, if none are specified by the user,
    * the default @Production and @Standard are used. For internal use.
    * 
    * @param enabledDeploymentTypes The enabled deployment types from
    *           web-beans.xml
    */
   protected void checkEnabledDeploymentTypes()
   {
      if (!this.enabledDeploymentTypes.get(0).equals(Standard.class))
      {
         throw new DeploymentException("@Standard must be the lowest precedence deployment type");
      }
   }

   protected void addWebBeansDeploymentTypes()
   {
      if (!this.enabledDeploymentTypes.contains(WebBean.class))
      {
         this.enabledDeploymentTypes.add(1, WebBean.class);
      }
   }

   /**
    * Registers a bean with the manager
    * 
    * @param bean The bean to register
    * @return A reference to manager
    * 
    * @see javax.enterprise.inject.spi.BeanManager#addBean(javax.inject.manager.Bean)
    */
   public void addBean(Bean<?> bean)
   {
      if (beans.contains(bean))
      {
         return;
      }
      resolver.clear();
      beans.add(bean);
      registerBeanNamespace(bean);
      for (BeanManagerImpl childActivity : childActivities)
      {
         childActivity.addBean(bean);
      }
      return;
   }

   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      Class<?> clazz = event.getClass();
      for (Annotation annotation : bindings)
      {
         if (!getServices().get(MetaDataCache.class).getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new IllegalArgumentException("Not a binding type " + annotation);
         }
      }
      HashSet<Annotation> bindingAnnotations = new HashSet<Annotation>(Arrays.asList(bindings));
      if (bindingAnnotations.size() < bindings.length)
      {
         throw new DuplicateBindingTypeException("Duplicate binding types: " + bindings);
      }
      checkEventType(clazz);
      return eventManager.getObservers(event, bindings);
   }
   
   private void checkEventType(Type eventType)
   {
      Type[] types;
      if (eventType instanceof Class)
      {
         types = Reflections.getActualTypeArguments((Class<?>) eventType);
      }
      else if (eventType instanceof ParameterizedType)
      {
         types = ((ParameterizedType) eventType).getActualTypeArguments();
      }
      else
      {
         throw new IllegalArgumentException("Event type " + eventType + " isn't a concrete type");
      }
      for (Type type : types)
      {
         if (type instanceof WildcardType)
         {
            throw new IllegalArgumentException("Cannot provide an event type parameterized with a wildcard " + eventType);
         }
         if (type instanceof TypeVariable)
         {
            throw new IllegalArgumentException("Cannot provide an event type parameterized with a type parameter " + eventType);
         }
      }
   }

   /**
    * A strongly ordered, unmodifiable list of enabled deployment types
    * 
    * @return The ordered enabled deployment types known to the manager
    */
   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return Collections.unmodifiableList(enabledDeploymentTypes);
   }

   /**
    * @return the enabledDecoratorClasses
    */
   public List<Class<?>> getEnabledDecoratorClasses()
   {
      return Collections.unmodifiableList(enabledDecoratorClasses);
   }

   /**
    * Set the enabled deployment types
    * 
    * @param enabledDeploymentTypes
    */
   public void setEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>(enabledDeploymentTypes);
      checkEnabledDeploymentTypes();
      addWebBeansDeploymentTypes();
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
      return getBeans(ResolvableWBClass.of(beanType, bindings, this), bindings);
   }
   
   public Set<Bean<?>> getBeans(WBAnnotated<?, ?> element, Annotation... bindings)
   {
      for (Annotation annotation : element.getAnnotations())
      {
         if (!getServices().get(MetaDataCache.class).getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new IllegalArgumentException("Not a binding type " + annotation);
         }
      }
      for (Type type : element.getActualTypeArguments())
      {
         if (type instanceof WildcardType)
         {
            throw new IllegalArgumentException("Cannot resolve a type parameterized with a wildcard " + element);
         }
         if (type instanceof TypeVariable)
         {
            throw new IllegalArgumentException("Cannot resolve a type parameterized with a type parameter " + element);
         }
      }
      if (bindings != null && bindings.length > element.getMetaAnnotations(BindingType.class).size())
      {
         throw new DuplicateBindingTypeException("Duplicate bindings (" + Arrays.asList(bindings) + ") type passed " + element.toString());
      }
      return resolver.get(ResolvableFactory.of(element));
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
         Set<Bean<?>> beans = getBeans(ResolvableWBClass.of(injectionPoint.getType(), injectionPoint.getBindings().toArray(new Annotation[0]), this));
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

   /**
    * Wraps a collection of beans into a thread safe list. Since this overwrites
    * any existing list of beans in the manager, this should only be done on
    * startup and other controlled situations. Also maps the beans by
    * implementation class. For internal use.
    * 
    * @param beans The set of beans to add
    * @return A reference to the manager
    */
   // TODO Build maps in the deployer :-)
   public void setBeans(Set<RIBean<?>> beans)
   {
      synchronized (beans)
      {
         for (RIBean<?> bean : beans)
         {
            if (bean instanceof NewEnterpriseBean)
            {
               newEnterpriseBeans.put(bean.getType(), (EnterpriseBean<?>) bean);
            }
            if (bean instanceof DecoratorBean)
            {
               decorators.add((DecoratorBean<?>) bean);
            }
            riBeans.put(bean.getId(), bean);
            registerBeanNamespace(bean);
            this.beans.add(bean);
         }
         resolver.clear();
      }
   }
   
   protected void registerBeanNamespace(Bean<?> bean)
   {
      if (bean.getName() != null && bean.getName().indexOf('.') > 0)
      {
         String name = bean.getName().substring(0, bean.getName().lastIndexOf('.'));
         String[] hierarchy = name.split("\\.");
         Namespace namespace = getRootNamespace();
         for (String s : hierarchy)
         {
            namespace = namespace.putIfAbsent(s);
         }
      }
   }

   /**
    * Gets the class-mapped beans. For internal use.
    * 
    * @return The bean map
    */
   public Map<Class<?>, EnterpriseBean<?>> getNewEnterpriseBeanMap()
   {
      return newEnterpriseBeans;
   }

   /**
    * The beans registered with the Web Bean manager. For internal use
    * 
    * @return The list of known beans
    */
   public List<Bean<?>> getBeans()
   {
      return Collections.unmodifiableList(beans);
   }
   
   public List<DecoratorBean<?>> getDecorators()
   {
      return Collections.unmodifiableList(decorators);
   }

   public Map<String, RIBean<?>> getRiBeans()
   {
      return Collections.unmodifiableMap(riBeans);
   }

   /**
    * Registers a context with the manager
    * 
    * @param context The context to add
    * @return A reference to the manager
    * 
    * @see javax.enterprise.inject.spi.BeanManager#addContext(javax.enterprise.context.spi.Context)
    */
   public void addContext(Context context)
   {
      contexts.put(context.getScopeType(), context);
   }

   public void addObserver(Observer<?> observer, Annotation... bindings)
   {
      addObserver(observer, eventManager.getTypeOfObserver(observer), bindings);
   }

   /**
    * Shortcut to register an ObserverImpl
    * 
    * @param <T>
    * @param observer
    */
   public <T> void addObserver(ObserverImpl<T> observer)
   {
      addObserver(observer, observer.getEventType(), observer.getBindingsAsArray());
   }

   public void addObserver(ObserverMethod<?, ?> observerMethod)
   {
      addObserver(observerMethod, observerMethod.getObservedEventType(), new ArrayList<Annotation>(observerMethod.getObservedEventBindings()).toArray(new Annotation[0]));
   }

   /**
    * Does the actual observer registration
    * 
    * @param observer
    * @param eventType
    * @param bindings
    * @return
    */
   public void addObserver(Observer<?> observer, Type eventType, Annotation... bindings)
   {
      checkEventType(eventType);
      this.eventManager.addObserver(observer, eventType, bindings);
      for (BeanManagerImpl childActivity : childActivities)
      {
         childActivity.addObserver(observer, eventType, bindings);
      }
   }
   
   public void removeObserver(Observer<?> observer)
   {
      eventManager.removeObserver(observer);
   }

   /**
    * Fires an event object with given event object for given bindings
    * 
    * @param event The event object to pass along
    * @param bindings The binding types to match
    * 
    * @see javax.enterprise.inject.spi.BeanManager#fireEvent(java.lang.Object,
    *      java.lang.annotation.Annotation[])
    */
   public void fireEvent(Object event, Annotation... bindings)
   {
      // Check the event object for template parameters which are not allowed by
      // the spec.
      if (Reflections.isParameterizedType(event.getClass()))
      {
         throw new IllegalArgumentException("Event type " + event.getClass().getName() + " is not allowed because it is a generic");
      }
      // Also check that the binding types are truly binding types
      for (Annotation binding : bindings)
      {
         if (!Reflections.isBindings(binding))
         {
            throw new IllegalArgumentException("Event type " + event.getClass().getName() + " cannot be fired with non-binding type " + binding.getClass().getName() + " specified");
         }
      }

      // Get the observers for this event. Although resolveObservers is
      // parameterized, this method is not, so we have to use
      // Observer<Object> for observers.
      Set<Observer<Object>> observers = resolveObservers(event, bindings);
      eventManager.notifyObservers(observers, event);
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
         throw new ContextNotActiveException("No active contexts for scope type " + scopeType.getName());
      }
      if (activeContexts.size() > 1)
      {
         throw new IllegalStateException("More than one context active for scope type " + scopeType.getName());
      }
      return activeContexts.iterator().next();

   }
   
   public Object getInjectableReference(Bean<?> bean, CreationalContext<?> creationalContext)
   {
      bean = getMostSpecializedBean(bean);
      if (getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isNormal())
      {
         if (creationalContext != null || (creationalContext == null && getContext(bean.getScopeType()).get(bean) != null))
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
         return getContext(bean.getScopeType()).get((Bean) bean, creationalContext);
      }
   }

   /*
    * TODO this is not correct, as the current implementation of getInstance
    * does not pay attention to what type the resulting instance needs to
    * implement
    */
   public Object getReference(Bean<?> bean, Type beanType)
   {
      return getInjectableReference(bean, CreationalContextImpl.of(bean));
   }

   @SuppressWarnings("unchecked")
   public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> creationalContext)
   {
      boolean registerInjectionPoint = !injectionPoint.getType().equals(InjectionPoint.class);
      try
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().push(injectionPoint);
         }
         WBAnnotated<?, ?> element = ResolvableWBClass.of(injectionPoint.getType(), injectionPoint.getBindings().toArray(new Annotation[0]), this);
         Bean<?> resolvedBean = getBean(element, element.getBindingsAsArray());
         if (getServices().get(MetaDataCache.class).getScopeModel(resolvedBean.getScopeType()).isNormal() && !Proxies.isTypeProxyable(injectionPoint.getType()))
         {
            throw new UnproxyableResolutionException("Attempting to inject an unproxyable normal scoped bean " + resolvedBean + " into " + injectionPoint);
         }
         if (creationalContext instanceof CreationalContextImpl)
         {
            CreationalContextImpl<?> ctx = (CreationalContextImpl<?>) creationalContext;
            if (ctx.containsIncompleteInstance(resolvedBean))
            {
               return ctx.getIncompleteInstance(resolvedBean);
            }
            else
            {
               return getInjectableReference(resolvedBean, ctx.getCreationalContext(resolvedBean));
            }
         }
         else
         {
            return getInjectableReference(resolvedBean, creationalContext);
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

   /**
    * Returns an instance by API type and binding types
    * 
    * @param type The API type to match
    * @param bindings The binding types to match
    * @return An instance of the bean
    * 
    * @deprecated replace with non-contextual injection
    * 
    */
   @Deprecated
   public <T> T getInstanceByType(Class<T> type, Annotation... bindings)
   {
      WBAnnotated<T, ?> element = ResolvableWBClass.of(type, bindings, this);
      return (T) getReference(getBean(element, bindings), type);
   }

   public <T> Bean<T> getBean(WBAnnotated<T, ?> element, Annotation... bindings)
   {
      Set<Bean<?>> beans = getBeans(element, bindings);
      if (beans.size() == 0)
      {
         throw new UnsatisfiedResolutionException(element + "Unable to resolve any Web Beans");
      }
      else if (beans.size() > 1)
      {
         throw new AmbiguousResolutionException(element + "Resolved multiple Web Beans");
      }
      Bean<T> bean = (Bean<T>) beans.iterator().next();
      boolean normalScoped = getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isNormal();
      if (normalScoped && !Beans.isBeanProxyable(bean))
      {
         throw new UnproxyableResolutionException("Normal scoped bean " + bean + " is not proxyable");
      }
      return bean;
   }

   public Set<Bean<?>> getBeans(String name)
   {
      return resolver.get(name);
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
      // TODO Fix this cast and make the resolver return a list
      return new ArrayList(decoratorResolver.get(ResolvableFactory.of(types, bindings)));
   }
   
   public List<Decorator<?>> resolveDecorators(Bean<?> bean)
   {
      throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
   }

   /**
    * Get the web bean resolver. For internal use
    * 
    * @return The resolver
    */
   public Resolver getResolver()
   {
      return resolver;
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
      buffer.append("Enabled deployment types: " + getEnabledDeploymentTypes() + "\n");
      buffer.append("Registered contexts: " + contexts.keySet() + "\n");
      buffer.append("Registered beans: " + getBeans().size() + "\n");
      buffer.append("Specialized beans: " + specializedBeans.size() + "\n");
      return buffer.toString();
   }

   public BeanManagerImpl createActivity()
   {
      BeanManagerImpl childActivity = newChildManager(this);
      childActivities.add(childActivity);
      CurrentManager.add(childActivity);
      return childActivity;
   }

   public BeanManagerImpl setCurrent(Class<? extends Annotation> scopeType)
   {
      if (!getServices().get(MetaDataCache.class).getScopeModel(scopeType).isNormal())
      {
         throw new IllegalArgumentException("Scope must be a normal scope type " + scopeType);
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
         return CurrentManager.rootManager();
      }
      else if (activeCurrentActivities.size() == 1)
      {
         return activeCurrentActivities.get(0).getManager();
      }
      throw new IllegalStateException("More than one current activity for an active context " + currentActivities);
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

   /**
    * Accesses the factory used to create each instance of InjectionPoint that
    * is injected into web beans.
    * 
    * @return the factory
    */
   public InjectionPoint getInjectionPoint()
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
    * 
    * @return
    */
   public Map<Bean<?>, Bean<?>> getSpecializedBeans()
   {
      // TODO make this unmodifiable after deploy!
      return specializedBeans;
   }

   // Serialization

   protected Object readResolve()
   {
      return CurrentManager.get(id);
   }

   /**
    * Provides access to the executor service used for asynchronous tasks.
    * 
    * @return the ExecutorService for this manager
    */
   public ExecutorService getTaskExecutor()
   {
      return taskExecutor;
   }

   public void shutdown()
   {
      log.trace("Ending application");
      shutdownExecutors();
      ApplicationContext.instance().destroy();
      ApplicationContext.instance().setActive(false);
      ApplicationContext.instance().setBeanStore(null);
      CurrentManager.cleanup();
   }

   /**
    * Shuts down any executor services in the manager.
    */
   protected void shutdownExecutors()
   {
      taskExecutor.shutdown();
      try
      {
         // Wait a while for existing tasks to terminate
         if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS))
         {
            taskExecutor.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS))
            {
               // Log the error here
            }
         }
      }
      catch (InterruptedException ie)
      {
         // (Re-)Cancel if current thread also interrupted
         taskExecutor.shutdownNow();
         // Preserve interrupt status
         Thread.currentThread().interrupt();
      }
   }
   
   protected ClientProxyProvider getClientProxyProvider()
   {
      return clientProxyProvider;
   }
   
   protected ConcurrentListMultiMap<Class<? extends Annotation>, Context> getContexts()
   {
      return contexts;
   }
   
   protected AtomicInteger getIds()
   {
      return ids;
   }
   
   protected Set<CurrentActivity> getCurrentActivities()
   {
      return currentActivities;
   }
   
   public Integer getId()
   {
      return id;
   }
   
   public ConcurrentSetMultiMap<Type, EventObserver<?>> getRegisteredObservers()
   {
      return registeredObservers;
   }
   
   public Namespace getRootNamespace()
   {
      return rootNamespace;
   }

   public <T> InjectionTarget<T> createInjectionTarget(Class<T> type)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <T> ManagedBean<T> createManagedBean(Class<T> type)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <T> ManagedBean<T> createManagedBean(AnnotatedType<T> type)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <X> Bean<? extends X> getMostSpecializedBean(Bean<X> bean)
   {
      Bean<?> key = bean;
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

   public void validate(InjectionPoint injectionPoint)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public Set<Annotation> getInterceptorBindingTypeDefinition(Class<? extends Annotation> bindingType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public Bean<?> getPassivationCapableBean(String id)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public ScopeType getScopeDefinition(Class<? extends Annotation> scopeType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public boolean isBindingType(Class<? extends Annotation> annotationType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public boolean isInterceptorBindingType(Class<? extends Annotation> annotationType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public boolean isScopeType(Class<? extends Annotation> annotationType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public boolean isStereotype(Class<? extends Annotation> annotationType)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <X> Bean<? extends X> getHighestPrecedenceBean(Set<Bean<? extends X>> beans)
   {
      if (beans.size() == 1)
      {
         return beans.iterator().next();
      }
      else if (beans.isEmpty())
      {
         return null;
      }

      // make a copy so that the sort is stable with respect to new deployment types added through the SPI
      // TODO This code needs to be in Resolver
      // TODO This needs caching
      final List<Class<? extends Annotation>> enabledDeploymentTypes = getEnabledDeploymentTypes();

      SortedSet<Bean<? extends X>> sortedBeans = new TreeSet<Bean<? extends X>>(new Comparator<Bean<? extends X>>()
      {
         public int compare(Bean<? extends X> o1, Bean<? extends X> o2)
         {
            int diff = enabledDeploymentTypes.indexOf(o1) - enabledDeploymentTypes.indexOf(o2);
            if (diff == 0)
            {
               throw new AmbiguousResolutionException();
            }
            return diff;
         }
            });
      sortedBeans.addAll(beans);
      return sortedBeans.last();
   }
   
   public ELResolver getELResolver()
   {
      return webbeansELResolver;
   }
   
}
