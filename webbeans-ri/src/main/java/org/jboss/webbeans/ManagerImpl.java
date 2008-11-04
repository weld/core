package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.webbeans.AmbiguousDependencyException;
import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.DeploymentException;
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

import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.ejb.EjbManager;
import org.jboss.webbeans.event.EventBus;
import org.jboss.webbeans.exceptions.NameResolutionLocation;
import org.jboss.webbeans.exceptions.TypesafeResolutionLocation;
import org.jboss.webbeans.injectable.Injectable;
import org.jboss.webbeans.injectable.ResolverInjectable;
import org.jboss.webbeans.util.ClientProxy;
import org.jboss.webbeans.util.MapWrapper;
import org.jboss.webbeans.util.ProxyPool;
import org.jboss.webbeans.util.Reflections;

public class ManagerImpl implements Manager
{

   private class ContextMap extends MapWrapper<Class<? extends Annotation>, List<Context>>
   {
      public ContextMap()
      {
         super(new HashMap<Class<? extends Annotation>, List<Context>>());
      }

      public List<Context> get(Class<? extends Annotation> key)
      {
         return (List<Context>) super.get(key);
      }

      public DependentContext getDependentContext()
      {
         return (DependentContext) get(Dependent.class).get(0);
      }
   }

   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   private ModelManager modelManager;
   private EjbManager ejbLookupManager;
   private EventBus eventBus;
   private ResolutionManager resolutionManager;
   private ContextMap contextMap;
   private ProxyPool proxyPool;
   private List<Bean<?>> beans;
   private Set<Decorator> decorators;
   private Set<Interceptor> interceptors;

   public ManagerImpl()
   {
      initEnabledDeploymentTypes(null);
      initContexts(null);
      this.modelManager = new ModelManager();
      this.ejbLookupManager = new EjbManager();
      this.beans = new CopyOnWriteArrayList<Bean<?>>();
      this.eventBus = new EventBus();
      this.resolutionManager = new ResolutionManager(this);
      this.proxyPool = new ProxyPool(this);
      this.decorators = new HashSet<Decorator>();
      this.interceptors = new HashSet<Interceptor>();
   }

   protected void initEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      if (enabledDeploymentTypes == null)
      {
         this.enabledDeploymentTypes.add(0, Standard.class);
         this.enabledDeploymentTypes.add(1, Production.class);
      }
      else
      {
         this.enabledDeploymentTypes.addAll(enabledDeploymentTypes);
         if (!this.enabledDeploymentTypes.get(0).equals(Standard.class))
         {
            throw new DeploymentException("@Standard must be the lowest precedence deployment type");
         }
      }
   }

   protected void initContexts(Context... contexts)
   {
      this.contextMap = new ContextMap();
      if (contexts == null)
      {
         addContext(new DependentContext());
         addContext(new RequestContext());
         addContext(new SessionContext());
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

   public Manager addBean(Bean<?> bean)
   {
      if (beans.contains(bean))
      {
         return this;
      }
      getResolutionManager().clear();
      beans.add(bean);
      return this;
   }

   public <T> void removeObserver(Observer<T> observer)
   {

   }

   public <T> Set<Method> resolveDisposalMethods(Class<T> apiType, Annotation... bindingTypes)
   {
      return new HashSet<Method>();
   }

   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      return (Set<Observer<T>>) eventBus.getObservers(event, bindings);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }

   public ModelManager getModelManager()
   {
      return this.modelManager;
   }

   public EjbManager getEjbManager()
   {
      return ejbLookupManager;
   }

   public <T> Set<Bean<T>> resolveByType(Class<T> type, Annotation... bindingTypes)
   {
      return resolveByType(new ResolverInjectable<T>(type, bindingTypes, getModelManager()));
   }

   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> apiType, Annotation... bindingTypes)
   {
      return resolveByType(new ResolverInjectable<T>(apiType, bindingTypes, getModelManager()));
   }

   private <T> Set<Bean<T>> resolveByType(Injectable<T, ?> injectable)
   {
      Set<Bean<T>> beans = getResolutionManager().get(injectable);

      if (beans == null)
      {
         return new HashSet<Bean<T>>();
      }
      else
      {
         return beans;
      }

   }

   public ResolutionManager getResolutionManager()
   {
      return resolutionManager;
   }

   /**
    * Wraps a collection of beans into a thread safe list.
    * Since this overwrites any existing list of beans in the manager,
    * this should only be done on startup and other controlled situations.
    * 
    * @param beans The collection of beans to wrap.
    */
   public Manager setBeans(Collection<Bean<?>> beans) {
      this.beans = new CopyOnWriteArrayList<Bean<?>>(beans);
      return this;
   }
   
   public List<Bean<?>> getBeans()
   {
      return beans;
   }

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

   public Manager addDecorator(Decorator decorator)
   {
      decorators.add(decorator);
      return this;
   }

   public Manager addInterceptor(Interceptor interceptor)
   {
      interceptors.add(interceptor);
      return this;
   }

   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventBus.addObserver(observer, eventType, bindings);
      return this;
   }

   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO Using the eventType TypeLiteral<T>, the Class<T> object must be
      // retrieved
      this.eventBus.addObserver(observer, (Class<T>) Reflections.getActualTypeArguments(eventType.getClass())[0], bindings);
      return this;
   }

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

   public <T> T getInstance(Bean<T> bean)
   {
      try
      {
         contextMap.getDependentContext().setActive(true);
         if (getModelManager().getScopeModel(bean.getScopeType()).isNormal())
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
         contextMap.getDependentContext().setActive(false);
      }
   }

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

   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new ResolverInjectable<T>(type, bindingTypes, getModelManager()));
   }

   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new ResolverInjectable<T>(type, bindingTypes, getModelManager()));
   }

   private <T> T getInstanceByType(Injectable<T, ?> injectable)
   {
      Set<Bean<T>> beans = resolveByType(injectable);
      if (beans.size() == 0)
      {
         throw new UnsatisfiedDependencyException(new TypesafeResolutionLocation(injectable) + "Unable to resolve any Web Beans");
      }
      else if (beans.size() > 1)
      {
         throw new AmbiguousDependencyException(new TypesafeResolutionLocation(injectable) + "Resolved multiple Web Beans");
      }
      else
      {
         Bean<T> bean = beans.iterator().next();
         if (getModelManager().getScopeModel(bean.getScopeType()).isNormal() && !ClientProxy.isProxyable(injectable.getType()))
         {
            throw new UnproxyableDependencyException(new TypesafeResolutionLocation(injectable) + "Unable to proxy");
         }
         else
         {
            return getInstance(bean);
         }
      }
   }

   public <T> Manager removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      this.eventBus.removeObserver(observer, eventType, bindings);
      return this;
   }

   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO The Class<T> for the event type must be retrieved from the
      // TypeLiteral<T> instance
      this.eventBus.removeObserver(observer, (Class<T>) Reflections.getActualTypeArguments(eventType.getClass())[0], bindings);
      return this;
   }

   public Set<Bean<?>> resolveByName(String name)
   {
      return getResolutionManager().get(name);
   }

   public List<Decorator> resolveDecorators(Set<Class<?>> types, Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
