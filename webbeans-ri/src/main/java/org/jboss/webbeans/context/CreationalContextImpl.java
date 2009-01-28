package org.jboss.webbeans.context;

import java.util.HashMap;
import java.util.Map;

import javax.context.CreationalContext;
import javax.inject.manager.Bean;

public class CreationalContextImpl<T> implements CreationalContext<T>
{
   
   private final Map<Bean<?>, Object> incompleteInstances;
   private final Bean<T> bean;
   
   public CreationalContextImpl(Bean<T> bean)
   {
      this.incompleteInstances = new HashMap<Bean<?>, Object>();
      this.bean = bean;
   }
   
   private CreationalContextImpl(Bean<T> bean, Map<Bean<?>, Object> incompleteInstances)
   {
      this.incompleteInstances = incompleteInstances;
      this.bean = bean;
   }
   
   public void push(T incompleteInstance)
   {
      incompleteInstances.put(bean, incompleteInstance);
   }
   
   public <S> CreationalContextImpl<S> getCreationalContext(Bean<S> bean)
   {
      return new CreationalContextImpl<S>(bean, incompleteInstances);
   }
   
   public <S> S getIncompleteInstance(Bean<S> bean)
   {
      return (S) incompleteInstances.get(bean);
   }
   
   public boolean containsIncompleteInstance(Bean<?> bean)
   {
      return incompleteInstances.containsKey(bean);
   }
   
}
