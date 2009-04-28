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

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.context.Context;
import javax.context.ContextNotActiveException;
import javax.context.CreationalContext;
import javax.event.Observer;
import javax.inject.AmbiguousDependencyException;
import javax.inject.BindingType;
import javax.inject.DeploymentException;
import javax.inject.DuplicateBindingTypeException;
import javax.inject.Production;
import javax.inject.Standard;
import javax.inject.TypeLiteral;
import javax.inject.UnproxyableDependencyException;
import javax.inject.UnsatisfiedDependencyException;
import javax.inject.manager.Bean;
import javax.inject.manager.Decorator;
import javax.inject.manager.InjectionPoint;
import javax.inject.manager.InterceptionType;
import javax.inject.manager.Interceptor;
import javax.inject.manager.Manager;

import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.proxy.ClientProxyProvider;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.CreationalContextImpl;
import org.jboss.webbeans.el.Namespace;
import org.jboss.webbeans.event.EventManager;
import org.jboss.webbeans.event.EventObserver;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.NonContextualInjector;
import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;
import org.jboss.webbeans.injection.resolution.Resolver;
import org.jboss.webbeans.introspector.AnnotatedItem;
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
public class ManagerImpl implements WebBeansManager, Serializable
{
   
   private static class CurrentActivity
   {
      
      private final Context context;
      private final ManagerImpl manager;      
      
      public CurrentActivity(Context context, ManagerImpl manager)
      {
         this.context = context;
         this.manager = manager;
      }

      public Context getContext()
      {
         return context;
      }
      
      public ManagerImpl getManager()
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
   
   private static final Log log = Logging.getLog(ManagerImpl.class);

   private static final long serialVersionUID = 3021562879133838561L;

   // The JNDI key to place the manager under
   public static final String JNDI_KEY = "java:app/Manager";
   
   /*
    * Application scoped services
    * ****************************
    */  
   private transient final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
   private transient final ServiceRegistry services;
   
   /*
    * Application scoped data structures
    * ***********************************
    */
   private transient List<Class<? extends Annotation>> enabledDeploymentTypes;
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
   private final transient NonContextualInjector nonContextualInjector;   
   
   /*
    * Activity scoped data structures
    * ********************************
    */
   private transient final ThreadLocal<Stack<InjectionPoint>> currentInjectionPoint;
   private transient List<Bean<?>> beans;
   private final transient Namespace rootNamespace;
   private final transient ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers;
   private final transient Set<ManagerImpl> childActivities;
   private final Integer id;
   
   
   /**
    * Create a new, root, manager
    * 
    * @param serviceRegistry
    * @return
    */
   public static ManagerImpl newRootManager(ServiceRegistry serviceRegistry)
   {
      List<Class<? extends Annotation>> defaultEnabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      defaultEnabledDeploymentTypes.add(0, Standard.class);
      defaultEnabledDeploymentTypes.add(1, Production.class);
      
      return new ManagerImpl(
            serviceRegistry, 
            new CopyOnWriteArrayList<Bean<?>>(), 
            new ConcurrentSetHashMultiMap<Type, EventObserver<?>>(),
            new Namespace(),
            new ConcurrentHashMap<Class<?>, EnterpriseBean<?>>(),
            new ConcurrentHashMap<String, RIBean<?>>(), 
            new ClientProxyProvider(), 
            new ConcurrentListHashMultiMap<Class<? extends Annotation>, Context>(),
            new CopyOnWriteArraySet<CurrentActivity>(),
            new HashMap<Bean<?>, Bean<?>>(),
            defaultEnabledDeploymentTypes,
            new AtomicInteger()
            );
   }
   
   /**
    * Create a new child manager
    * 
    * @param parentManager
    * @return
    */
   public static ManagerImpl newChildManager(ManagerImpl parentManager)
   {
      List<Bean<?>> beans = new CopyOnWriteArrayList<Bean<?>>();
      beans.addAll(parentManager.getBeans());
      
      ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers = new ConcurrentSetHashMultiMap<Type, EventObserver<?>>();
      registeredObservers.deepPutAll(parentManager.getRegisteredObservers());
      Namespace rootNamespace = new Namespace(parentManager.getRootNamespace());
      
      return new ManagerImpl(
            parentManager.getServices(),
            beans,
            registeredObservers,
            rootNamespace,
            parentManager.getNewEnterpriseBeanMap(), 
            parentManager.getRiBeans(),
            parentManager.getClientProxyProvider(),
            parentManager.getContexts(),
            parentManager.getCurrentActivities(),
            parentManager.getSpecializedBeans(),
            parentManager.getEnabledDeploymentTypes(),
            parentManager.getIds()
            );
   }

   /**
    * Create a new manager
    * 
    * @param ejbServices the ejbResolver to use
    */
   private ManagerImpl(
         ServiceRegistry serviceRegistry, 
         List<Bean<?>> beans,
         ConcurrentSetMultiMap<Type, EventObserver<?>> registeredObservers,
         Namespace rootNamespace,
         Map<Class<?>, EnterpriseBean<?>> newEnterpriseBeans, 
         Map<String, RIBean<?>> riBeans,
         ClientProxyProvider clientProxyProvider,
         ConcurrentListMultiMap<Class<? extends Annotation>, Context> contexts,
         Set<CurrentActivity> currentActivities,
         Map<Bean<?>, Bean<?>> specializedBeans,
         List<Class<? extends Annotation>> enabledDeploymentTypes,
         AtomicInteger ids
         )
   {
      this.services = serviceRegistry;
      this.beans = beans;
      this.newEnterpriseBeans = newEnterpriseBeans;
      this.riBeans = riBeans;
      this.clientProxyProvider = clientProxyProvider;
      this.contexts = contexts;
      this.currentActivities = currentActivities;
      this.specializedBeans = specializedBeans;
      this.registeredObservers = registeredObservers;
      setEnabledDeploymentTypes(enabledDeploymentTypes);
      this.rootNamespace = rootNamespace;
      this.ids = ids;
      this.id = ids.incrementAndGet();
      
      this.resolver = new Resolver(this);
      this.eventManager = new EventManager(this);
      this.nonContextualInjector = new NonContextualInjector(this);
      this.childActivities = new CopyOnWriteArraySet<ManagerImpl>();
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
    * @see javax.inject.manager.Manager#addBean(javax.inject.manager.Bean)
    */
   public Manager addBean(Bean<?> bean)
   {
      if (beans.contains(bean))
      {
         return this;
      }
      resolver.clear();
      beans.add(bean);
      registerBeanNamespace(bean);
      for (ManagerImpl childActivity : childActivities)
      {
         childActivity.addBean(bean);
      }
      return this;
   }

   /**
    * Resolve the disposal method for the given producer method. For internal
    * use.
    * 
    * @param apiType The API type to match
    * @param bindings The binding types to match
    * @return The set of matching disposal methods
    */
   public <T> Set<DisposalMethodBean<T>> resolveDisposalBeans(Class<T> apiType, Annotation... bindings)
   {
      // Correct?
      Set<Bean<T>> beans = resolveByType(apiType, bindings);
      Set<DisposalMethodBean<T>> disposalBeans = new HashSet<DisposalMethodBean<T>>();
      for (Bean<T> bean : beans)
         if (bean instanceof DisposalMethodBean)
            disposalBeans.add((DisposalMethodBean<T>) bean);
      return disposalBeans;
   }

   /**
    * Resolves observers for given event and bindings
    * 
    * @param event The event to match
    * @param bindings The binding types to match
    * @return The set of matching observers
    * 
    * @see javax.inject.manager.Manager#resolveObservers(java.lang.Object,
    *      java.lang.annotation.Annotation[])
    */
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

   /**
    * Resolves beans by API type and binding types
    * 
    * @param type The API type to match
    * @param bindings The binding types to match
    * @return The set of matching beans
    * 
    * @see javax.inject.manager.Manager#resolveByType(java.lang.Class,
    *      java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindings)
   {
      return resolveByType(ResolvableAnnotatedClass.of(type, bindings), bindings);
   }

   /**
    * Resolves beans by API type literal and binding types
    * 
    * @param type The API type literal to match
    * @param bindings The binding types to match
    * @return The set of matching beans
    * 
    * @see javax.inject.manager.Manager#resolveByType(javax.inject.TypeLiteral,
    *      java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> type, Annotation... bindings)
   {
      return resolveByType(ResolvableAnnotatedClass.of(type, bindings), bindings);
   }

   public <T> Set<Bean<T>> resolveByType(AnnotatedItem<T, ?> element, InjectionPoint injectionPoint, Annotation... bindings)
   {
      boolean registerInjectionPoint = !injectionPoint.getType().equals(InjectionPoint.class);
      try
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().push(injectionPoint);
         }
         return resolveByType(element, bindings);
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
    * Check the resolution request is valid, and then ask the resolver to
    * perform the resolution. For internal use.
    * 
    * @param element The item to resolve
    * @param bindings The binding types to match
    * @return The set of matching beans
    */
   public <T> Set<Bean<T>> resolveByType(AnnotatedItem<T, ?> element, Annotation... bindings)
   {
      for (Annotation annotation : element.getAnnotationsAsSet())
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
      if (bindings.length > element.getMetaAnnotations(BindingType.class).size())
      {
         throw new DuplicateBindingTypeException("Duplicate bindings (" + Arrays.asList(bindings) + ") type passed " + element.toString());
      }
      return resolver.get(element);
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
         this.beans = new CopyOnWriteArrayList<Bean<?>>(beans);
         for (RIBean<?> bean : beans)
         {
            if (bean instanceof NewEnterpriseBean)
            {
               newEnterpriseBeans.put(bean.getType(), (EnterpriseBean<?>) bean);
            }
            riBeans.put(bean.getId(), bean);
            registerBeanNamespace(bean);
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
    * @see javax.inject.manager.Manager#addContext(javax.context.Context)
    */
   public Manager addContext(Context context)
   {
      contexts.put(context.getScopeType(), context);
      return this;
   }

   /**
    * Registers a decorator with the manager
    * 
    * @param decorator The decorator to register
    * @return A reference to the manager
    * 
    * @see javax.inject.manager.Manager#addDecorator(javax.inject.manager.Decorator)
    */
   public Manager addDecorator(Decorator decorator)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   /**
    * Registers an interceptor with the manager
    * 
    * @param interceptor The interceptor to register
    * @return A reference to the manager
    * 
    * @see javax.inject.manager.Manager#addInterceptor(javax.inject.manager.Interceptor)
    */
   public Manager addInterceptor(Interceptor interceptor)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      return addObserverByType(observer, eventType, bindings);
   }

   /**
    * Shortcut to register an ObserverImpl
    * 
    * @param <T>
    * @param observer
    * @return
    */
   public <T> Manager addObserver(ObserverImpl<T> observer)
   {
      return addObserverByType(observer, observer.getEventType(), observer.getBindingsAsArray());
   }

   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      return addObserverByType(observer, eventType.getType(), bindings);
   }
   
   /**
    * Does the actual observer registration
    *  
    * @param <T>
    * @param observer
    * @param eventType
    * @param bindings
    * @return
    */
   public <T> Manager addObserverByType(Observer<T> observer, Type eventType, Annotation... bindings)
   {
      checkEventType(eventType);
      this.eventManager.addObserver(observer, eventType, bindings);
      for (ManagerImpl childActivity : childActivities)
      {
         childActivity.addObserverByType(observer, eventType, bindings);
      }
      return this;
   }

   /**
    * Fires an event object with given event object for given bindings
    * 
    * @param event The event object to pass along
    * @param bindings The binding types to match
    * 
    * @see javax.inject.manager.Manager#fireEvent(java.lang.Object,
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
    * @see javax.inject.manager.Manager#getContext(java.lang.Class)
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

   /**
    * Returns an instance of a bean
    * 
    * @param bean The bean to instantiate
    * @return An instance of the bean
    * 
    * @see javax.inject.manager.Manager#getInstance(javax.inject.manager.Bean)
    */
   public <T> T getInstance(Bean<T> bean)
   {
      return getInstance(bean, true);
   }

   public <T> T getInstance(Bean<T> bean, boolean create)
   {
      if (create)
      {
         return getInstance(bean, new CreationalContextImpl<T>(bean));
      }
      else
      {
         return getInstance(bean, null);
      }
   }

   /**
    * Returns an instance of a bean
    * 
    * @param bean The bean to instantiate
    * @return An instance of the bean
    * 
    * @see javax.inject.manager.Manager#getInstance(javax.inject.manager.Bean)
    */
   @SuppressWarnings("unchecked")
   private <T> T getInstance(Bean<T> bean, CreationalContextImpl<T> creationalContext)
   {
      if (specializedBeans.containsKey(bean))
      {
         return getInstance((Bean<T>) specializedBeans.get(bean), creationalContext);
      }
      else if (getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isNormal())
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
         return getContext(bean.getScopeType()).get(bean, creationalContext);
      }
   }

   public <T> T getInstanceToInject(InjectionPoint injectionPoint)
   {
      return this.<T> getInstanceToInject(injectionPoint, null);
   }

   public void injectNonContextualInstance(Object instance)
   {
      nonContextualInjector.inject(instance);
   }

   @SuppressWarnings("unchecked")
   public <T> T getInstanceToInject(InjectionPoint injectionPoint, CreationalContext<?> creationalContext)
   {
      boolean registerInjectionPoint = !injectionPoint.getType().equals(InjectionPoint.class);
      try
      {
         if (registerInjectionPoint)
         {
            currentInjectionPoint.get().push(injectionPoint);
         }
         AnnotatedItem<T, ?> element = ResolvableAnnotatedClass.of(injectionPoint.getType(), injectionPoint.getBindings().toArray(new Annotation[0]));
         Bean<T> resolvedBean = getBeanByType(element, element.getBindingsAsArray());
         if (getServices().get(MetaDataCache.class).getScopeModel(resolvedBean.getScopeType()).isNormal() && !Proxies.isTypeProxyable(injectionPoint.getType()))
         {
            throw new UnproxyableDependencyException("Attempting to inject an unproxyable normal scoped bean " + resolvedBean + " into " + injectionPoint);
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
               return getInstance(resolvedBean, ctx.getCreationalContext(resolvedBean));
            }
         }
         else
         {
            return getInstance(resolvedBean);
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
    * Gets an instance by name, returning null if none is found and throwing an
    * exception if too many beans match
    * 
    * @param name The name to match
    * @return An instance of the bean
    * 
    * @see javax.inject.manager.Manager#getInstanceByName(java.lang.String)
    */
   public Object getInstanceByName(String name)
   {
      Set<Bean<?>> beans = resolveByName(name);
      if (beans.size() == 0)
      {
         return null;
      }
      else if (beans.size() > 1)
      {
         throw new AmbiguousDependencyException("Resolved multiple Web Beans with " + name);
      }
      else
      {
         return getInstance(beans.iterator().next());
      }
   }

   /**
    * Returns an instance by API type and binding types
    * 
    * @param type The API type to match
    * @param bindings The binding types to match
    * @return An instance of the bean
    * 
    * @see javax.inject.manager.Manager#getInstanceByType(java.lang.Class,
    *      java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(Class<T> type, Annotation... bindings)
   {
      return getInstanceByType(ResolvableAnnotatedClass.of(type, bindings), bindings);
   }

   /**
    * Returns an instance by type literal and binding types
    * 
    * @param type The type to match
    * @param bindings The binding types to match
    * @return An instance of the bean
    * 
    * @see javax.inject.manager.Manager#getInstanceByType(javax.inject.TypeLiteral,
    *      java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindings)
   {
      return getInstanceByType(ResolvableAnnotatedClass.of(type, bindings), bindings);
   }

   /**
    * Resolve an instance, verify that the resolved bean can be instantiated,
    * and return
    * 
    * @param element The annotated item to match
    * @param bindings The binding types to match
    * @return An instance of the bean
    */
   public <T> T getInstanceByType(AnnotatedItem<T, ?> element, Annotation... bindings)
   {
      return getInstance(getBeanByType(element, bindings));
   }

   public <T> Bean<T> getBeanByType(AnnotatedItem<T, ?> element, Annotation... bindings)
   {
      Set<Bean<T>> beans = resolveByType(element, bindings);
      if (beans.size() == 0)
      {
         throw new UnsatisfiedDependencyException(element + "Unable to resolve any Web Beans");
      }
      else if (beans.size() > 1)
      {
         throw new AmbiguousDependencyException(element + "Resolved multiple Web Beans");
      }
      Bean<T> bean = beans.iterator().next();
      boolean normalScoped = getServices().get(MetaDataCache.class).getScopeModel(bean.getScopeType()).isNormal();
      if (normalScoped && !Beans.isBeanProxyable(bean))
      {
         throw new UnproxyableDependencyException("Normal scoped bean " + bean + " is not proxyable");
      }
      return bean;
   }

   /**
    * Removes an observer
    * 
    * @param observer The observer to remove
    * @param eventType The event type to match
    * @param bindings the binding types to match
    * @return A reference to the manager
    * 
    * @see javax.inject.manager.Manager#removeObserver(javax.event.Observer,
    *      java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventManager.removeObserver(observer, eventType, bindings);
      return this;
   }

   /**
    * Removes an observer
    * 
    * @param observer The observer to remove
    * @param eventType The event type to match
    * @param bindings the binding types to match
    * @return A reference to the manager
    * 
    * @see javax.inject.manager.Manager#removeObserver(javax.event.Observer,
    *      javax.inject.TypeLiteral, java.lang.annotation.Annotation[])
    */
   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      this.eventManager.removeObserver(observer, eventType.getRawType(), bindings);
      return this;
   }

   /**
    * Resolves a set of beans based on their name
    * 
    * @param The name to match
    * @return The set of matching beans
    * 
    * @see javax.inject.manager.Manager#resolveByName(java.lang.String)
    */
   public Set<Bean<?>> resolveByName(String name)
   {
      return resolver.get(name);
   }

   /**
    * Resolves a list of decorators based on API types and binding types Os
    * 
    * @param types The set of API types to match
    * @param bindings The binding types to match
    * @return A list of matching decorators
    * 
    * @see javax.inject.manager.Manager#resolveDecorators(java.util.Set,
    *      java.lang.annotation.Annotation[])
    */
   public List<Decorator> resolveDecorators(Set<Type> types, Annotation... bindings)
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
    * @see javax.inject.manager.Manager#resolveInterceptors(javax.inject.manager.InterceptionType,
    *      java.lang.annotation.Annotation[])
    */
   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
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

   public Manager parse(InputStream xmlStream)
   {
      throw new UnsupportedOperationException();
   }

   public ManagerImpl createActivity()
   {
      ManagerImpl childActivity = newChildManager(this);
      childActivities.add(childActivity);
      CurrentManager.add(childActivity);
      return childActivity;
   }

   public ManagerImpl setCurrent(Class<? extends Annotation> scopeType)
   {
      if (!getServices().get(MetaDataCache.class).getScopeModel(scopeType).isNormal())
      {
         throw new IllegalArgumentException("Scope must be a normal scope type " + scopeType);
      }
      currentActivities.add(new CurrentActivity(getContext(scopeType), this));
      return this;
   }
   
   public ManagerImpl getCurrent()
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

}
