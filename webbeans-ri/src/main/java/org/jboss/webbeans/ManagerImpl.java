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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.BindingType;
import javax.webbeans.ContextNotActiveException;
import javax.webbeans.DeploymentException;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Observer;
import javax.webbeans.Production;
import javax.webbeans.Standard;
import javax.webbeans.TypeLiteral;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Decorator;
import javax.webbeans.manager.InterceptionType;
import javax.webbeans.manager.Interceptor;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.proxy.ProxyPool;
import org.jboss.webbeans.contexts.ContextMap;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.event.EventManager;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.resources.spi.Naming;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

/**
 * Implementation of the Web Beans Manager.
 * 
 * Essentially a singleton for registering Beans, Contexts, Observers,
 * Interceptors etc. as well as providing resolution
 * 
 * @author Pete Muir
 * 
 */
@Standard
public class ManagerImpl implements Manager, Serializable
{
   private static final long serialVersionUID = 1L;

   // The JNDI key to place the manager under
   public static final String JNDI_KEY = "java:comp/Manager";

   // The enabled deployment types from web-beans.xml
   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   // The Web Beans manager
   private EventManager eventManager;
   // The bean resolver
   private Resolver resolver;
   // The registered contexts
   private ContextMap contextMap;
   // The client proxy pool
   private ProxyPool proxyPool;
   // The registered beans
   private List<Bean<?>> beans;
   // The registered beans, mapped by implementation class
   private Map<Class<?>, Bean<?>> beanMap;
   // The registered decorators
   private Set<Decorator> decorators;
   // The registered interceptors
   private Set<Interceptor> interceptors;

   private EjbDescriptorCache ejbDescriptorCache;
   
   // The Naming (JNDI) access
   private Naming naming;

   /**
    * Constructor
    * 
    * @param enabledDeploymentTypes any enabled deployment types, an empty set
    *           if none are specified
    */
   public ManagerImpl()
   {
      this.beans = new CopyOnWriteArrayList<Bean<?>>();
      this.beanMap = new ConcurrentHashMap<Class<?>, Bean<?>>();
      this.resolver = new Resolver(this);
      this.proxyPool = new ProxyPool();
      this.decorators = new HashSet<Decorator>();
      this.interceptors = new HashSet<Interceptor>();
      this.contextMap = new ContextMap();
      this.eventManager = new EventManager();
      this.ejbDescriptorCache = new EjbDescriptorCache();

      List<Class<? extends Annotation>> defaultEnabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      defaultEnabledDeploymentTypes.add(0, Standard.class);
      defaultEnabledDeploymentTypes.add(1, Production.class);
      setEnabledDeploymentTypes(defaultEnabledDeploymentTypes);
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

   /**
    * Registers a bean with the manager
    * 
    * @param bean The bean to register
    * @return A reference to manager
    * 
    * @see javax.webbeans.manager.Manager#addBean(javax.webbeans.manager.Bean)
    */
   public Manager addBean(Bean<?> bean)
   {
      if (beans.contains(bean))
      {
         return this;
      }
      resolver.clear();
      beans.add(bean);
      return this;
   }

   /**
    * Resolve the disposal method for the given producer method. For internal
    * use.
    * 
    * @param apiType The API type to match
    * @param bindingTypes The binding types to match
    * @return The set of matching disposal methods
    */
   public <T> Set<AnnotatedMethod<Object>> resolveDisposalMethods(Class<T> apiType, Annotation... bindings)
   {
      return new HashSet<AnnotatedMethod<Object>>();
   }

   /**
    * Resolves observers for given event and bindings
    * 
    * @param event The event to match
    * @param bindings The binding types to match
    * @return The set of matching observers
    * 
    * @see javax.webbeans.manager.Manager#resolveObservers(java.lang.Object,
    *      java.lang.annotation.Annotation[])
    */
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      return eventManager.getObservers(event, bindings);
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
      this.enabledDeploymentTypes = enabledDeploymentTypes;
      checkEnabledDeploymentTypes();
   }

   /**
    * Resolves beans by API type and binding types
    * 
    * @param type The API type to match
    * @param bindingTypes The binding types to match
    * @return The set of matching beans
    * 
    * @see javax.webbeans.manager.Manager#resolveByType(java.lang.Class,
    *      java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindings)
   {
      return resolveByType(new AnnotatedClassImpl<T>(type, type, bindings), bindings);
   }

   /**
    * Resolves beans by API type literal and binding types
    * 
    * @param type The API type literal to match
    * @param bindingTypes The binding types to match
    * @return The set of matching beans
    * 
    * @see javax.webbeans.manager.Manager#resolveByType(javax.webbeans.TypeLiteral,
    *      java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> type, Annotation... bindings)
   {
      return resolveByType(new AnnotatedClassImpl<T>(type.getRawType(), type.getType(), bindings), bindings);
   }

   /**
    * Check the resolution request is valid, and then ask the resolver to
    * perform the resolution. For internal use.
    * 
    * @param element The item to resolve
    * @param bindingTypes The binding types to match
    * @return The set of matching beans
    */
   public <T> Set<Bean<T>> resolveByType(AnnotatedItem<T, ?> element, Annotation... bindings)
   {
      for (Annotation annotation : element.getAnnotations())
      {
         if (!MetaDataCache.instance().getBindingTypeModel(annotation.annotationType()).isValid())
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
         throw new DuplicateBindingTypeException(element.toString());
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
   public void setBeans(Set<AbstractBean<?, ?>> beans)
   {
      synchronized (beans)
      {
         this.beans = new CopyOnWriteArrayList<Bean<?>>(beans);
         for (AbstractBean<?, ?> bean : beans)
         {
            beanMap.put(bean.getType(), bean);
         }
         resolver.clear();
      }
   }

   /**
    * Gets the class-mapped beans. For internal use.
    * 
    * @return The bean map
    */
   public Map<Class<?>, Bean<?>> getBeanMap()
   {
      return beanMap;
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

   /**
    * Registers a context with the manager
    * 
    * @param context The context to add
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#addContext(javax.webbeans.manager.Context)
    */
   public Manager addContext(Context context)
   {
      contextMap.add(context);
      return this;
   }

   /**
    * Registers a decorator with the manager
    * 
    * @param decorator The decorator to register
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#addDecorator(javax.webbeans.manager.Decorator)
    */
   public Manager addDecorator(Decorator decorator)
   {
      decorators.add(decorator);
      return this;
   }

   /**
    * Registers an interceptor with the manager
    * 
    * @param interceptor The interceptor to register
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#addInterceptor(javax.webbeans.manager.Interceptor)
    */
   public Manager addInterceptor(Interceptor interceptor)
   {
      interceptors.add(interceptor);
      return this;
   }

   /**
    * Registers an observer for a given event type and binding types
    * 
    * @param observer The observer to register
    * @param eventType The event type to match
    * @param bindings The bindings to match
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#addObserver(javax.webbeans.Observer,
    *      java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventManager.addObserver(observer, eventType, bindings);
      return this;
   }

   /**
    * Registers an observer for a given event type literal and binding types
    * 
    * @param observer The observer to register
    * @param eventType The event type literal to match
    * @param bindings The bindings to match
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#addObserver(javax.webbeans.Observer,
    *      javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   @SuppressWarnings("unchecked")
   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      eventManager.addObserver(observer, (Class<T>) eventType.getType(), bindings);
      return this;
   }

   /**
    * Fires an event object with given event object for given bindings
    * 
    * @param event The event object to pass along
    * @param bindings The binding types to match
    * 
    * @see javax.webbeans.manager.Manager#fireEvent(java.lang.Object,
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
         if (!Reflections.isBindingType(binding))
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
    * @see javax.webbeans.manager.Manager#getContext(java.lang.Class)
    */
   public Context getContext(Class<? extends Annotation> scopeType)
   {
      List<Context> activeContexts = new ArrayList<Context>();
      for (Context context : contextMap.getContext(scopeType))
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
         throw new IllegalArgumentException("More than one context active for scope type " + scopeType.getName());
      }
      return activeContexts.iterator().next();
   }

   /**
    * Direct access to built in contexts. For internal use.
    * 
    * @param scopeType The scope type of the context
    * @return The context
    */
   public Context getBuiltInContext(Class<? extends Annotation> scopeType)
   {
      return contextMap.getBuiltInContext(scopeType);
   }

   /**
    * Returns an instance of a bean
    * 
    * @param bean The bean to instantiate
    * @return An instance of the bean
    * 
    * @see javax.webbeans.manager.Manager#getInstance(javax.webbeans.manager.Bean)
    */
   public <T> T getInstance(Bean<T> bean)
   {
      if (MetaDataCache.instance().getScopeModel(bean.getScopeType()).isNormal())
      {
         return (T) proxyPool.getClientProxy(bean, true);
      }
      else
      {
         return getContext(bean.getScopeType()).get(bean, true);
      }
   }

   /**
    * Gets an instance by name, returning null if none is found and throwing an
    * exception if too many beans match
    * 
    * @param name The name to match
    * @return An instance of the bean
    * 
    * @see javax.webbeans.manager.Manager#getInstanceByName(java.lang.String)
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
    * @param bindingTypes The binding types to match
    * @return An instance of the bean
    * 
    * @see javax.webbeans.manager.Manager#getInstanceByType(java.lang.Class,
    *      java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(Class<T> type, Annotation... bindings)
   {
      return getInstanceByType(new AnnotatedClassImpl<T>(type, type, bindings), bindings);
   }

   public <T> T getMostSpecializedInstance(Bean<T> bean, boolean create)
   {
      // TODO Implement specialization
      if (MetaDataCache.instance().getScopeModel(bean.getScopeType()).isNormal())
      {
         return (T) proxyPool.getClientProxy(bean, create);
      }
      else
      {
         return getContext(bean.getScopeType()).get(bean, create);
      }
   }

   /**
    * Returns an instance by type literal and binding types
    * 
    * @param type The type to match
    * @param bindingTypes The binding types to match
    * @return An instance of the bean
    * 
    * @see javax.webbeans.manager.Manager#getInstanceByType(javax.webbeans.TypeLiteral,
    *      java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindings)
   {
      return getInstanceByType(new AnnotatedClassImpl<T>(type.getRawType(), type.getType(), bindings), bindings);
   }

   /**
    * Resolve an instance, verify that the resolved bean can be instantiated,
    * and return
    * 
    * @param element The annotated item to match
    * @param bindingTypes The binding types to match
    * @return An instance of the bean
    */
   public <T> T getInstanceByType(AnnotatedItem<T, ?> element, Annotation... bindings)
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
      else
      {
         return getInstance(beans.iterator().next());
      }
   }

   /**
    * Removes an observer
    * 
    * @param observer The observer to remove
    * @param eventType The event type to match
    * @param bindings the binding types to match
    * @return A reference to the manager
    * 
    * @see javax.webbeans.manager.Manager#removeObserver(javax.webbeans.Observer,
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
    * @see javax.webbeans.manager.Manager#removeObserver(javax.webbeans.Observer,
    *      javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   @SuppressWarnings("unchecked")
   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      this.eventManager.removeObserver(observer, (Class<T>) eventType.getType(), bindings);
      return this;
   }

   /**
    * Resolves a set of beans based on their name
    * 
    * @param The name to match
    * @return The set of matching beans
    * 
    * @see javax.webbeans.manager.Manager#resolveByName(java.lang.String)
    */
   public Set<Bean<?>> resolveByName(String name)
   {
      return resolver.get(name);
   }

   /**
    * Resolves a list of decorators based on API types and binding types
    * 
    * @param types The set of API types to match
    * @param bindingTypes The binding types to match
    * @return A list of matching decorators
    * 
    * @see javax.webbeans.manager.Manager#resolveDecorators(java.util.Set,
    *      java.lang.annotation.Annotation[])
    */
   public List<Decorator> resolveDecorators(Set<Class<?>> types, Annotation... bindings)
   {
      return resolver.resolveDecorators(types, bindings);
   }

   /**
    * Resolves a list of interceptors based on interception type and interceptor
    * bindings
    * 
    * @param type The interception type to resolve
    * @param interceptorBindings The binding types to match
    * @return A list of matching interceptors
    * 
    * @see javax.webbeans.manager.Manager#resolveInterceptors(javax.webbeans.manager.InterceptionType,
    *      java.lang.annotation.Annotation[])
    */
   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
   {
      return resolver.resolveInterceptors(type, interceptorBindings);
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

   public EjbDescriptorCache getEjbDescriptorCache()
   {
      return ejbDescriptorCache;
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
      buffer.append("Registered contexts: " + contextMap.keySet() + "\n");
      buffer.append("Registered beans: " + getBeans().size() + "\n");
      buffer.append("Registered decorators: " + decorators.size() + "\n");
      buffer.append("Registered interceptors: " + interceptors.size() + "\n");
      return buffer.toString();
   }

   public String toDetailedString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Strings.collectionToString("Enabled deployment types: ", getEnabledDeploymentTypes()));
      buffer.append(eventManager.toString() + "\n");
      buffer.append(MetaDataCache.instance().toString() + "\n");
      buffer.append(resolver.toString() + "\n");
      buffer.append(contextMap.toString() + "\n");
      buffer.append(proxyPool.toString() + "\n");
      buffer.append(Strings.collectionToString("Registered beans: ", getBeans()));
      buffer.append(Strings.collectionToString("Registered decorators: ", decorators));
      buffer.append(Strings.collectionToString("Registered interceptors: ", interceptors));
      return buffer.toString();
   }

   public Manager parse(InputStream xmlStream)
   {
      // TODO Implement XML parsing
      return null;
   }

   public Manager validate()
   {
      // TODO Implement XML parsing
      return null;
   }

   public Manager createChildManager()
   {
      // TODO Implement hierarchical managers
      return null;
   }

   public Manager setCurrent()
   {
      // TODO Implement hierarchical managers
      return null;
   }
   
   public Naming getNaming()
   {
      return naming;
   }
   
   public void setNaming(Naming naming)
   {
      this.naming = naming;
   }

}
