package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;

public class BeanDeployerEnvironment
{
   
   private static final AnnotatedItem<?, ?> OTHER_BEANS_ANNOTATED_ITEM = ResolvableAnnotatedClass.of(BeanDeployerEnvironment.class, new Annotation[0]);
   
   private final Map<AnnotatedItem<?, ?>, Set<RIBean<?>>> beanMap;
   private final Set<ObserverImpl<?>> observers;
   
   public BeanDeployerEnvironment()
   {
      this.beanMap = new HashMap<AnnotatedItem<?,?>, Set<RIBean<?>>>();
      this.observers = new HashSet<ObserverImpl<?>>();
   }
   
   public Map<AnnotatedItem<?, ?>, Set<RIBean<?>>> getBeanMap()
   {
      return beanMap;
   }
   
   public void addBean(AnnotatedItem<?, ?> key, RIBean<?> value)
   {
      if (key == null)
      {
         key = OTHER_BEANS_ANNOTATED_ITEM;
      }
      if (!beanMap.containsKey(key))
      {
         beanMap.put(key, new HashSet<RIBean<?>>());
      }
      beanMap.get(key).add(value);
   }
   
   public Set<RIBean<?>> getBeans()
   {
      Set<RIBean<?>> beans = new HashSet<RIBean<?>>();
      for (Entry<AnnotatedItem<?, ?>, Set<RIBean<?>>> entry : beanMap.entrySet())
      {
         beans.addAll(entry.getValue());
      }
      return beans;
   }
   
   public Set<ObserverImpl<?>> getObservers()
   {
      return observers;
   }
   
}
