package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.binding.NewBinding;
import org.jboss.webbeans.introspector.AnnotatedItem;

public class NewBean<T> extends ForwardingBean<T>
{
   private AbstractClassBean<T> wrappedBean;
   
   public NewBean(AbstractClassBean<T> wrappedBean, Manager manager)
   {
      super(manager);
      this.wrappedBean = wrappedBean;
   }

   @Override
   protected Bean<T> delegate()
   {
      return wrappedBean;
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return Dependent.class;
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return Standard.class;
   }

   @Override
   public String getName()
   {
      return null;
   }

   public Set<AnnotatedItem<?, ?>> getInjectionPoints()
   {
      return wrappedBean.getInjectionPoints();
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return new HashSet<Annotation>(Arrays.asList(new NewBinding()));
   }
   
   
}
