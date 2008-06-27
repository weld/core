package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.Context;
import javax.webbeans.Observer;
import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.bindings.ProductionBinding;
import org.jboss.webbeans.bindings.StandardBinding;

public class ContainerImpl implements Container
{
   
   private List<Annotation> enabledDeploymentTypes;
   private StereotypeManager stereotypeManager;
   
   public ContainerImpl()
   {
      initEnabledDeploymentTypes();
      this.stereotypeManager = new StereotypeManager();
   }
   
   private void initEnabledDeploymentTypes()
   {
      this.enabledDeploymentTypes = new ArrayList<Annotation>();
      // TODO Support enabling custom deployment types
      if (this.enabledDeploymentTypes.size() == 0)
      {
         this.enabledDeploymentTypes.add(0, new StandardBinding());
         this.enabledDeploymentTypes.add(1, new ProductionBinding());
      }
   }

   public Container addComponent(ComponentInstance component)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void addContext(Context context)
   {
      // TODO Auto-generated method stub
      
   }

   public void addObserver(Observer observer)
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

   public void removeObserver(Observer observer)
   {
      // TODO Auto-generated method stub
      
   }

   public Set<ComponentInstance> resolveByName(String name)
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

   public <T> Set<Observer<T>> resolveObservers(T event, Annotation... bindings)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public List<Annotation> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
   public StereotypeManager getStereotypeManager()
   {
      return this.stereotypeManager;
   }
   
}
