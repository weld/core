package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.AmbiguousDependencyException;
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

import org.jboss.webbeans.ejb.EjbManager;
import org.jboss.webbeans.event.EventBus;
import org.jboss.webbeans.exceptions.NameResolutionLocation;
import org.jboss.webbeans.exceptions.TypesafeResolutionLocation;
import org.jboss.webbeans.injectable.Injectable;
import org.jboss.webbeans.injectable.SimpleInjectable;
import org.jboss.webbeans.util.ClientProxy;

public class ManagerImpl implements Manager
{
   
   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   private ModelManager modelManager;
   private EjbManager ejbLookupManager;
   private EventBus eventBus;
   private ResolutionManager resolutionManager;

   private Set<Bean<?>> beans;

   public ManagerImpl(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      initEnabledDeploymentTypes(enabledDeploymentTypes);
      this.modelManager = new ModelManager();
      this.ejbLookupManager = new EjbManager();
      this.beans = new HashSet<Bean<?>>();
      this.eventBus = new EventBus();
      this.resolutionManager = new ResolutionManager(this);
   }

   private void initEnabledDeploymentTypes(
         List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      this.enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      if (enabledDeploymentTypes == null)
      {
         this.enabledDeploymentTypes.add(0, Standard.class);
         this.enabledDeploymentTypes.add(1, Production.class);
      } else
      {
         this.enabledDeploymentTypes.addAll(enabledDeploymentTypes);
         if (!this.enabledDeploymentTypes.get(0).equals(
               Standard.class))
         {
            throw new DeploymentException(
                  "@Standard must be the lowest precedence deployment type");
         }
      }
   }

   public Manager addBean(Bean<?> bean)
   {
      getResolutionManager().clear();
      beans.add(bean);
      return this;
   }

   public <T> void removeObserver(Observer<T> observer)
   {

   }

   public <T> Set<Method> resolveDisposalMethods(Class<T> apiType,
         Annotation... bindingTypes)
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

   public <T> Set<Bean<T>> resolveByType(Class<T> type,
         Annotation... bindingTypes)
   {
      return resolveByType(new SimpleInjectable<T>(type, bindingTypes));
   }

   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> apiType,
         Annotation... bindingTypes)
   {
      return resolveByType(new SimpleInjectable<T>(apiType, bindingTypes));
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

   public Set<Bean<?>> getBeans()
   {
      return beans;
   }

   public Manager addContext(Context context)
   {
      // TODO Auto-generated method stub
      return this;
   }

   public Manager addDecorator(Decorator decorator)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Manager addInterceptor(Interceptor interceptor)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> Manager addObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings)
   {
      // TODO Auto-generated method stub
      return this;
   }

   public <T> Manager addObserver(Observer<T> observer, TypeLiteral<T> eventType,
         Annotation... bindings)
   {
      // TODO Auto-generated method stub
      return this;
   }

   public void fireEvent(Object event, Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public Context getContext(Class<? extends Annotation> scopeType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T getInstance(Bean<T> bean)
   {
      if (getModelManager().getScopeModel(bean.getScopeType()).isNormal())
      {
         // TODO return a client proxy
         return null;
      }
      else
      {
         return getContext(bean.getScopeType()).get(bean, true);
      }
   }

   public Object getInstanceByName(String name)
   {
      Set<Bean<?>> beans = resolveByName(name);
      if (beans.size() == 0)
      {
         throw new UnsatisfiedDependencyException(new NameResolutionLocation(name) + "Unable to resolve any Web Beans");
      }
      else if (beans.size() > 1)
      {
         throw new AmbiguousDependencyException(new NameResolutionLocation(name) + "Resolved multiple Web Beans");
      }
      else
      {
         return beans.iterator().next();
      }
   }

   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new SimpleInjectable<T>(type, bindingTypes));
   }

   public <T> T getInstanceByType(TypeLiteral<T> type, Annotation... bindingTypes)
   {
      return getInstanceByType(new SimpleInjectable<T>(type, bindingTypes));
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
      // TODO Auto-generated method stub
      return this;
   }

   public <T> Manager removeObserver(Observer<T> observer, TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO Auto-generated method stub
      return this;
   }

   public Set<Bean<?>> resolveByName(String name)
   {
      return getResolutionManager().get(name);
   }

   public List<Decorator> resolveDecorators(Set<Class<?>> types,
         Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<Interceptor> resolveInterceptors(InterceptionType type,
         Annotation... interceptorBindings)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
