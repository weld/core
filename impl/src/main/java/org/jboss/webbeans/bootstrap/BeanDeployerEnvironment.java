package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.resolution.ResolvableAnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;

public class BeanDeployerEnvironment
{

   private static final AnnotatedItem<?, ?> OTHER_BEANS_ANNOTATED_ITEM = ResolvableAnnotatedClass.of(BeanDeployerEnvironment.class, new Annotation[0]);

   private final Map<AnnotatedClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<AnnotatedMethod<?>, ProducerMethodBean<?>> producerMethodBeanMap;
   private final Map<AnnotatedMethod<?>, DisposalMethodBean<?>> disposalMethodBeanMap;
   private final Set<RIBean<?>> beans;
   private final Set<ObserverImpl<?>> observers;
   private final Set<DisposalMethodBean<?>> allDisposalBeans;
   private final Set<DisposalMethodBean<?>> resolvedDisposalBeans;
   private final EjbDescriptorCache ejbDescriptors;

   public BeanDeployerEnvironment(EjbDescriptorCache ejbDescriptors)
   {
      this.classBeanMap = new HashMap<AnnotatedClass<?>, AbstractClassBean<?>>();
      this.producerMethodBeanMap = new HashMap<AnnotatedMethod<?>, ProducerMethodBean<?>>();
      this.disposalMethodBeanMap = new HashMap<AnnotatedMethod<?>, DisposalMethodBean<?>>();
      this.allDisposalBeans = new HashSet<DisposalMethodBean<?>>();
      this.resolvedDisposalBeans = new HashSet<DisposalMethodBean<?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.observers = new HashSet<ObserverImpl<?>>();
      this.ejbDescriptors = ejbDescriptors;
   }

   public ProducerMethodBean<?> getProducerMethod(AnnotatedMethod<?> method)
   {
      if (!producerMethodBeanMap.containsKey(method))
      {
         return null;
      }
      else
      {
         ProducerMethodBean<?> bean = producerMethodBeanMap.get(method);
         bean.initialize(this);
         return bean;
      }
   }

   public AbstractClassBean<?> getClassBean(AnnotatedClass<?> clazz)
   {
      if (!classBeanMap.containsKey(clazz))
      {
         return null;
      }
      else
      {
         AbstractClassBean<?> bean = classBeanMap.get(clazz);
         bean.initialize(this);
         return bean;
      }
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
          producerMethodBeanMap.put(bean.getAnnotatedItem(), bean);
      } else if (value instanceof DisposalMethodBean) {
    	  DisposalMethodBean<?> bean = (DisposalMethodBean<?>) value;
    	  disposalMethodBeanMap.put(bean.getAnnotatedItem(), bean);
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

   public Set<DisposalMethodBean<?>> getAllDisposalBeans()
   {
      return allDisposalBeans;
   }

   public void addAllDisposalBean(DisposalMethodBean<?> disposalBean)
   {
      allDisposalBeans.add(disposalBean);
   }

   public void addResolvedDisposalBean(DisposalMethodBean<?> disposalBean)
   {
      resolvedDisposalBeans.add(disposalBean);
   }

   public Set<DisposalMethodBean<?>> getResolvedDisposalBeans()
   {
      return resolvedDisposalBeans;
   }
   
   public EjbDescriptorCache getEjbDescriptors() 
   {
	  return ejbDescriptors;
   }

}
