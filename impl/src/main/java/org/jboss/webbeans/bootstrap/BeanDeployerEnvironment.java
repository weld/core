package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class BeanDeployerEnvironment
{
   
   private static final AnnotatedItem<?, ?> OTHER_BEANS_ANNOTATED_ITEM = ResolvableAnnotatedClass.of(BeanDeployerEnvironment.class, new Annotation[0]);
   
   private final Map<AnnotatedClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<AnnotatedMethod<?>, ProducerMethodBean<?>> methodBeanMap; 
   private final Set<RIBean<?>> beans;
   private final Set<ObserverImpl<?>> observers;
   private final Set<AnnotatedMethod<?>> disposalMethods; 
    
   public BeanDeployerEnvironment()
   {
      this.classBeanMap = new HashMap<AnnotatedClass<?>, AbstractClassBean<?>>();
      this.methodBeanMap = new HashMap<AnnotatedMethod<?>, ProducerMethodBean<?>>();
      this.disposalMethods = new HashSet<AnnotatedMethod<?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.observers = new HashSet<ObserverImpl<?>>();
   }
   
   public Map<AnnotatedClass<?>, AbstractClassBean<?>> getClassBeanMap()
   {
      return Collections.unmodifiableMap(classBeanMap);
   }
   
   public Map<AnnotatedMethod<?>, ProducerMethodBean<?>> getMethodBeanMap()
   {
      return Collections.unmodifiableMap(methodBeanMap);
   }
   
   public void addBean(RIBean<?> value)
   {
      if (value instanceof AbstractClassBean && !(value instanceof NewBean))
      {
         AbstractClassBean<?> bean = (AbstractClassBean<?>) value;
         classBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      else if (value instanceof ProducerMethodBean)
      {
         ProducerMethodBean<?> bean = (ProducerMethodBean<?>) value;
         methodBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      beans.add(value);
   }
   
   public Set<RIBean<?>> getBeans()
   {
      return Collections.unmodifiableSet(beans);
   }
   
   public Set<ObserverImpl<?>> getObservers()
   {
      return observers;
   }
   
   public Set<AnnotatedMethod<?>> getDisposalMethods()
   {
      return disposalMethods;
   }
   
}
