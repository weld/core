package org.jboss.webbeans.injectable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedParameter;

public abstract class Invokable<T, S>
{
   
   private List<InjectableParameter<Object>> parameters;
   
   protected Invokable() {}
   
   public Invokable(List<AnnotatedParameter<Object>> parameters)
   {
      this.parameters = new ArrayList<InjectableParameter<Object>>();
      for (AnnotatedParameter<Object> parameter : parameters)
      {
         this.parameters.add(new InjectableParameter<Object>(parameter));
      }
   }
   
   public abstract S getAnnotatedItem();
   
   protected Object[] getParameterValues(ManagerImpl manager)
   {
      Object[] parameterValues = new Object[parameters.size()];
      Iterator<InjectableParameter<Object>> iterator = parameters.iterator();   
      for (int i = 0; i < parameterValues.length; i++)
      {
         parameterValues[i] = iterator.next().getValue(manager);
      }
      return parameterValues;
   }
   
   public List<InjectableParameter<Object>> getParameters()
   {
      return parameters;
   }
   
   public abstract T invoke(Manager container, Object instance, Object[] parameters);
   
   public abstract T invoke(ManagerImpl container, Object instance);
   
}
