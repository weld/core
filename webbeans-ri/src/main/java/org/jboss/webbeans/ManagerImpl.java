package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Standard;
import javax.webbeans.TypeLiteral;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;
import javax.webbeans.manager.Observer;

import org.jboss.webbeans.bindings.ProductionAnnotationLiteral;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.ejb.EjbManager;
import org.jboss.webbeans.event.EventBus;

public class ManagerImpl implements Manager
{
   
   private List<Annotation> enabledDeploymentTypes;
   private ModelManager modelManager;
   private EjbManager ejbLookupManager;
   private EventBus eventBus;

   
   private ThreadLocal<Map<Class<Annotation>, Context>> contexts = 
      new ThreadLocal<Map<Class<Annotation>, Context>>();

   private Set<Bean<?>> components;
   
   public ManagerImpl(List<Annotation> enabledDeploymentTypes)
   {
      initEnabledDeploymentTypes(enabledDeploymentTypes);
      this.modelManager = new ModelManager();
      this.ejbLookupManager = new EjbManager();
      this.components = new HashSet<Bean<?>>();
      this.eventBus = new EventBus();
   }
   
   private void initEnabledDeploymentTypes(List<Annotation> enabledDeploymentTypes)
   {
      this.enabledDeploymentTypes = new ArrayList<Annotation>();
      if (enabledDeploymentTypes == null)
      {
         this.enabledDeploymentTypes.add(0, new StandardAnnotationLiteral());
         this.enabledDeploymentTypes.add(1, new ProductionAnnotationLiteral());
      }
      else
      {
         this.enabledDeploymentTypes.addAll(enabledDeploymentTypes);
         if (!this.enabledDeploymentTypes.get(0).annotationType().equals(Standard.class))
         {
            throw new RuntimeException("@Standard must be the lowest precedence deployment type");
         }
      }
   }

   public Manager addBean(Bean<?> component)
   {
      components.add(component);
      return this;
   }

   public void addContext(Context context)
   {
      // TODO Auto-generated method stub
      
   }

   public <T> void addObserver(Observer<T> observer)
   {
      eventBus.addObserver(observer);      
   }

   public void fireEvent(Object event, Annotation... bindings)
   {
      // TODO Auto-generated method stub
      
   }

   public Context getContext(Class<Annotation> scopeType)
   {
      Context context = contexts.get().get(scopeType);
      
      if (context == null)
      {
         // If context can't be found throw an exception (section 9.4 of spec)
         throw new ContextNotActiveException();         
      }
      else
      {
         return context;
      }
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

   public <T> T getInstanceByType(TypeLiteral<T> type,
         Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public<T> void removeObserver(Observer<T> observer)
   {
      eventBus.removeObserver(observer);
   }

   public Set<Bean<?>> resolveByName(String name)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T resolveByType(Class<T> apiType, Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T resolveByType(TypeLiteral<T> apiType,
         Annotation... bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public <T> Set<Method> resolveDisposalMethods(Class<T> apiType, Annotation... bindingTypes)
   {
      return new HashSet<Method>();
   }

   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      return (Set<Observer<T>>) eventBus.getObservers(event, bindings);
   }
   
   public List<Annotation> getEnabledDeploymentTypes()
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

   public <T> T getInstance(Bean<T> bean)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
