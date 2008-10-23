package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.webbeans.DeploymentException;
import javax.webbeans.Production;
import javax.webbeans.Standard;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Decorator;
import javax.webbeans.manager.InterceptionType;
import javax.webbeans.manager.Interceptor;
import javax.webbeans.manager.Manager;
import javax.webbeans.Observer;

import org.jboss.webbeans.ejb.EjbManager;
import org.jboss.webbeans.event.EventBus;
import org.jboss.webbeans.injectable.SimpleInjectable;

public class ManagerImpl implements Manager
{

   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   private ModelManager modelManager;
   private EjbManager ejbLookupManager;
   private EventBus eventBus;
   private ResolutionManager resolutionManager;

   private ThreadLocal<Map<Class<Annotation>, Context>> contexts = new ThreadLocal<Map<Class<Annotation>, Context>>();

   private Set<Bean<?>> beans;

   public ManagerImpl(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      initEnabledDeploymentTypes(enabledDeploymentTypes);
      this.modelManager = new ModelManager();
      this.ejbLookupManager = new EjbManager();
      this.beans = new HashSet<Bean<?>>();
      this.eventBus = new EventBus();
      resolutionManager = new ResolutionManager(this);
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

   public <T> Set<Bean<T>> resolveByType(Class<T> apiType,
         Annotation... bindingTypes)
   {
      return getResolutionManager().get(
            new SimpleInjectable<T>(apiType, bindingTypes));
   }

   public <T> Set<Bean<T>> resolveByType(TypeLiteral<T> apiType,
         Annotation... bindingTypes)
   {
      return resolveByType(apiType.getRawType(), bindingTypes);
   }

   public ResolutionManager getResolutionManager()
   {
      return resolutionManager;
   }

   public Set<Bean<?>> getBeans()
   {
      return beans;
   }

   public void addContext(Context context)
   {
      // TODO Auto-generated method stub

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

   public <T> void addObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public <T> void addObserver(Observer<T> observer, TypeLiteral<T> eventType,
         Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public void fireEvent(Object event, Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public Context getContext(Class<Annotation> scopeType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T getInstance(Bean<T> bean)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getInstanceByName(String name)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T getInstanceByType(Class<T> type, Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public <T> T getInstanceByType(Class<T> type, Set<Annotation> bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T getInstanceByType(TypeLiteral<T> type,
         Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> void removeObserver(Observer<T> observer, Class<T> eventType,
         Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public <T> void removeObserver(Observer<T> observer,
         TypeLiteral<T> eventType, Annotation... bindings)
   {
      // TODO Auto-generated method stub

   }

   public Set<Bean<?>> resolveByName(String name)
   {
      // TODO Auto-generated method stub
      return null;
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
