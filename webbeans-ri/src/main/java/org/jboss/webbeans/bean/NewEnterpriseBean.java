package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.binding.NewBinding;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;

public class NewEnterpriseBean<T> extends EnterpriseBean<T>
{
   private static Set<Annotation> NEW_BINDING_SET = new HashSet<Annotation>(Arrays.asList(new NewBinding()));

   public static <T> NewEnterpriseBean<T> of(AnnotatedClass<T> clazz, ManagerImpl manager)
   {
      return new NewEnterpriseBean<T>(clazz, manager);
   }

   public static <T> NewEnterpriseBean<T> of(Class<T> clazz, ManagerImpl manager)
   {
      return of(AnnotatedClassImpl.of(clazz), manager);
   }

   protected NewEnterpriseBean(AnnotatedClass<T> type, ManagerImpl manager)
   {
      super(type, manager);
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

   @Override
   public Set<AnnotatedMethod<Object>> getProducerMethods()
   {
      return Collections.emptySet();
   }

   @Override
   public Set<Annotation> getBindings()
   {
      return NEW_BINDING_SET;
   }

}
