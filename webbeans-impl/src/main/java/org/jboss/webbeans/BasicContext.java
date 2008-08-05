package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;
import javax.webbeans.Context;

/**
 * Basic implementation of javax.webbeans.Context, backed by a HashMap
 * 
 * @author Shane Bryzak
 *
 */
public class BasicContext implements Context
{
   private Map<ComponentInstance<?>, Object> values;
   private Class<? extends Annotation> scopeType;
   
   public BasicContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      values = new HashMap<ComponentInstance<?>,Object>();
   }
   
   @SuppressWarnings("unchecked")
   public <T> T get(Container container, ComponentInstance<T> component, boolean create) 
   {
      T instance = (T) values.get(component);
      if (instance != null)
      {
         return instance;
      }
      
      if (!create)
      {
         return null;
      }
      
      // TODO should component creation be synchronized?
      
      instance = component.create(container);
      
      values.put(component, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType() 
   {
      return scopeType;
   }

   @SuppressWarnings("unchecked")
   public <T> void remove(Container container, ComponentInstance<T> component) 
   {
      T instance = (T) values.get(component);
      
      if (instance != null)
      {
         values.remove(component);
         component.destroy(container, instance);
      }
      else
      {
         // TODO is this the correct exception to throw? See section 9.1 of spec
         throw new RuntimeException("Component " + component.getName() + " cannot be removed as it " + 
               "does not exist in [" + scopeType + "] context.");
      }
   }
   
   @SuppressWarnings("unchecked")
   public void destroy(Container container)
   {
      // TODO this method isn't declared by the interface, but is implied by section 9.1.2 of the spec
      
      for (ComponentInstance c : values.keySet())
      {
         c.destroy(container, values.get(c));
      }
      
      values.clear();
   }

}
