package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.BindingType;
import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentException;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Observer;
import javax.webbeans.Production;
import javax.webbeans.Standard;
import javax.webbeans.TypeLiteral;
import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.UnsatisfiedDependencyException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Decorator;
import javax.webbeans.manager.InterceptionType;
import javax.webbeans.manager.Interceptor;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bean.proxy.ProxyPool;
import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.ContextMap;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.ejb.DefaultEnterpriseBeanLookup;
import org.jboss.webbeans.event.EventBus;
import org.jboss.webbeans.exceptions.NameResolutionLocation;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.Reflections;

/**
 * Implementation of the Web Beans Manager.
 * 
 * Essentially a singleton for registering Beans, Contexts, Observers, 
 * Interceptors etc. as well as providing resolution
 * 
 * @author Pete Muir
 *
 */
public class ManagerImpl implements Manager
{
   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   private MetaDataCache metaDataCache;
   private EventBus eventBus;
   private Resolver resolver;
   private ContextMap contextMap;
   private ProxyPool proxyPool;
   private List<Bean<?>> beans;
   private Set<Decorator> decorators;
   private Set<Interceptor> interceptors;

   public ManagerImpl()
   {
      this.metaDataCache = new MetaDataCache();
      this.beans = new CopyOnWriteArrayList<Bean<?>>();
      this.eventBus = new EventBus();
      this.resolver = new Resolver(this);
      this.proxyPool = new ProxyPool(this);
      this.decorators = new HashSet<Decorator>();
      this.interceptors = new HashSet<Interceptor>();
      initEnabledDeploymentTypes();
      initContexts();
      initStandardBeans();
   }
   
   /**
    * Add any beans provided by the Web Beans RI to the registry
    */
   protected void initStandardBeans()
   {
      addBean( new SimpleBean<DefaultEnterpriseBeanLookup>( DefaultEnterpriseBeanLookup.class, this ) );
   }

   /**
    * Set up the enabled deployment types, if none are specified by the user,
    * the default @Production and @Standard are used
    */
   protected void initEnabledDeploymentTypes(Class<? extends Annotation> ... enabledDeploymentTypes)
   {
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      if (enabledDeploymentTypes.length == 0)
      {
         this.enabledDeploymentTypes.add(0, Standard.class);
         this.enabledDeploymentTypes.add(1, Production.class);
      }
      else
      {
         for (Class<? extends Annotation> enabledDeploymentType : enabledDeploymentTypes)
         {
            this.enabledDeploymentTypes.add(enabledDeploymentType);
         }
         if (!this.enabledDeploymentTypes.get(0).equals(Standard.class))
         {
            throw new DeploymentException("@Standard must be the lowest precedence deployment type");
         }
      }
   }

   /**
    * Set up the contexts. By default, the built in contexts are set up, but a
    * mock ManagerImpl may override this method to allow tests to set up 
    * other contexts
    */
   protected void initContexts(Context... contexts)
   {
      this.contextMap = new ContextMap();
      if (contexts.length == 0)
      {
         addContext(new DependentContext());
         addContext(new RequestContext());
         addContext(new SessionContext(this));
         addContext(new ApplicationContext());
      }
      else
      {
         for (Context context : contexts)
         {
            addContext(context);
         }
      }
   }

   /**
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
    * Resolve the disposal method for the given producer method
    * @param <T>
    * @param apiType
    * @param bindingTypes
    * @return
    */
   public <T> Set<AnnotatedMethod<Object>> resolveDisposalMethods(Class<T> apiType, Annotation... bindingTypes)
   {
      return new HashSet<AnnotatedMethod<Object>>();
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveObservers(java.lang.Object, java.lang.annotation.Annotation[])
    */
   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      return (Set<Observer<T>>) eventBus.getObservers(event, bindings);
   }

   /**
    * A strongly ordered list of enabled deployment types
    */
   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
   public MetaDataCache getMetaDataCache()
   {
      return this.metaDataCache;
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveByType(java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindingTypes)
   {
      return resolveByType(new AnnotatedClassImpl<T>(type, type, bindingTypes), bindingTypes);
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveByType(javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> type, Annotation... bindingTypes)
   {
      return resolveByType(new AnnotatedClassImpl<T>(type.getRawType(), type.getType(), bindingTypes), bindingTypes);
   }
   
   /**
    * Check the resolution request is valid, and then ask the resolver to 
    * perform the resolution
    */
   public <T> Set<Bean<T>> resolveByType(AnnotatedItem<T, ?> element, Annotation... bindingTypes)
   {
      for (Annotation annotation : element.getAnnotations())
      {
         if (!metaDataCache.getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new IllegalArgumentException("Not a binding type " + annotation);
         }
      }
      if (bindingTypes.length > element.getMetaAnnotations(BindingType.class).size())
      {
         throw new DuplicateBindingTypeException(element.toString());
      }
      return resolver.get(element);
   }

   /**
    * Wraps a collection of beans into a thread safe list.
    * Since this overwrites any existing list of beans in the manager,
    * this should only be done on startup and other controlled situations.
    * 
    */
   public Manager setBeans(Set<AbstractBean<?, ?>> beans) {
      this.beans = new CopyOnWriteArrayList<Bean<?>>(beans);
      resolver.clear();
      initStandardBeans();
      return this;
   }
   
   /**
    * The beans registered with the Web Bean manager
    */
   public List<Bean<?>> getBeans()
   {
      return beans;
   }

   /**
    * @see javax.webbeans.manager.Manager#addContext(javax.webbeans.manager.Context)
    */
   public Manager addContext(Context context)
   {
      List<Context> contexts = contextMap.get(context.getScopeType());
      if (contexts == null)
      {
         contexts = new ArrayList<Context>();
         contextMap.put(context.getScopeType(), contexts);
      }
      contexts.add(context);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#addDecorator(javax.webbeans.manager.Decorator)
    */
   public Manager addDecorator(Decorator decorator)
   {
      decorators.add(decorator);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#addInterceptor(javax.webbeans.manager.Interceptor)
    */
   public Manager addInterceptor(Interceptor interceptor)
   {
      interceptors.add(interceptor);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#addObserver(javax.webbeans.Observer, java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventBus.addObserver(observer, eventType, bindings);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#addObserver(javax.webbeans.Observer, javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO Using the eventType TypeLiteral<T>, the Class<T> object must be
      // retrieved
      this.eventBus.addObserver(observer, (Class<T>) Reflections.getActualTypeArguments(eventType.getClass())[0], bindings);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#fireEvent(java.lang.Object, java.lang.annotation.Annotation[])
    */
   public void fireEvent(Object event, Annotation... bindings)
   {
      // Check the event object for template parameters which are not allowed by
      // the spec.
      if (Reflections.isParameterizedType(event.getClass()))
      {
         throw new IllegalArgumentException("Event type " + event.getClass().getName() + " is not allowed because it is a generic");
      }
      // Get the observers for this event. Although resolveObservers is
      // parameterized, this
      // method is not, so we have to use Observer<Object> for observers.
      Set<Observer<Object>> observers = this.resolveObservers(event, bindings);
      this.eventBus.notifyObservers(observers, event);
   }

   /**
    * @see javax.webbeans.manager.Manager#getContext(java.lang.Class)
    */
   public Context getContext(Class<? extends Annotation> scopeType)
   {
      List<Context> contexts = contextMap.get(scopeType);
      if (contexts == null)
      {
         throw new ContextNotActiveException("No active contexts for " + scopeType.getName());
      }
      List<Context> activeContexts = new ArrayList<Context>();
      for (Context context : contexts)
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
      return activeContexts.get(0);
   }

   /**
    * @see javax.webbeans.manager.Manager#getInstance(javax.webbeans.manager.Bean)
    */
   public <T> T getInstance(Bean<T> bean)
   {
      try
      {
         contextMap.getBuiltInContext(Dependent.class).setActive(true);
         if (getMetaDataCache().getScopeModel(bean.getScopeType()).isNormal())
         {
            return (T) proxyPool.getClientProxy(bean);
         }
         else
         {
            return getContext(bean.getScopeType()).get(bean, true);
         }
      }
      finally
      {
         contextMap.getBuiltInContext(Dependent.class).setActive(false);
      }
   }

   /**
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
         throw new AmbiguousDependencyException(new NameResolutionLocation(name) + "Resolved multiple Web Beans");
      }
      else
      {
         return getInstance(beans.iterator().next());
      }
   }

   /**
    * @see javax.webbeans.manager.Manager#getInstanceByType(java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new AnnotatedClassImpl<T>(type, type, bindingTypes), bindingTypes);
   }

   /**
    * @see javax.webbeans.manager.Manager#getInstanceByType(javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new AnnotatedClassImpl<T>(type.getRawType(), type.getType(), bindingTypes), bindingTypes);
   }

   /**
    * Resolve an instance, verify that the resolved bean can be instantiated,
    * and return
    * 
    */
   public <T> T getInstanceByType(AnnotatedItem<T, ?> element, Annotation... bindingTypes)
   {
      Set<Bean<T>> beans = resolveByType(element, bindingTypes);
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
         Bean<T> bean = beans.iterator().next();
         if (getMetaDataCache().getScopeModel(bean.getScopeType()).isNormal() && !element.isProxyable())
         {
            throw new UnproxyableDependencyException(element + "Unable to proxy");
         }
         else
         {
            return getInstance(bean);
         }
      }
   }

   /**
    * @see javax.webbeans.manager.Manager#removeObserver(javax.webbeans.Observer, java.lang.Class, java.lang.annotation.Annotation[])
    */
   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventBus.removeObserver(observer, eventType, bindings);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#removeObserver(javax.webbeans.Observer, javax.webbeans.TypeLiteral, java.lang.annotation.Annotation[])
    */
   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO The Class<T> for the event type must be retrieved from the
      // TypeLiteral<T> instance
      this.eventBus.removeObserver(observer, (Class<T>) Reflections.getActualTypeArguments(eventType.getClass())[0], bindings);
      return this;
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveByName(java.lang.String)
    */
   public Set<Bean<?>> resolveByName(String name)
   {
      return resolver.get(name);
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveDecorators(java.util.Set, java.lang.annotation.Annotation[])
    */
   public List<Decorator> resolveDecorators(Set<Class<?>> types, Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see javax.webbeans.manager.Manager#resolveInterceptors(javax.webbeans.manager.InterceptionType, java.lang.annotation.Annotation[])
    */
   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   /**
    * Get the web bean resolver
    * @return
    */
   public Resolver getResolver()
   {
      return resolver;
   }

}
