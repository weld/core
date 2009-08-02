package org.jboss.webbeans.test.unit.activities.child;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.literal.CurrentLiteral;
import org.jboss.webbeans.manager.api.WebBeansManager;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class SameBeanTypeInChildActivityTest extends AbstractWebBeansTest
{
   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>();

   static
   {
      DEFAULT_BINDINGS.add(new CurrentLiteral());
   }

   private Bean<?> createDummyBean(BeanManager beanManager)
   {
      final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
      final Set<Type> types = new HashSet<Type>();
      final Set<Annotation> bindings = new HashSet<Annotation>();
      bindings.add(new AnnotationLiteral<SpecialBindingType>() {});
      types.add(Object.class);
      final Bean<?> bean = new Bean<MyBean>()
      {

         public Set<Annotation> getBindings()
         {
            return bindings;
         }

         public Set<InjectionPoint> getInjectionPoints()
         {
            return injectionPoints;
         }

         public String getName()
         {
            return null;
         }

         public Class<? extends Annotation> getScopeType()
         {
            return Dependent.class;
         }

         public Set<Type> getTypes()
         {
            return types;
         }

         public boolean isNullable()
         {
            return false;
         }

         public MyBean create(CreationalContext<MyBean> creationalContext)
         {
            return null;
         }

         public void destroy(MyBean instance, CreationalContext<MyBean> creationalContext)
         {

         }

         public Class<?> getBeanClass()
         {
            return MyBean.class;
         }

         public boolean isPolicy()
         {
            return false;
         }

         public Set<Class<? extends Annotation>> getStereotypes()
         {
            return Collections.emptySet();
         }

      };
      return bean;
   }

   @Test(groups = { "broken" }, expectedExceptions = { InjectionException.class })
   public void testSameBeanTypeInChildAsParentInjection()
   {
      BeanManager childActivity = getCurrentManager().createActivity();
      Bean<?> anotherMyBean = createDummyBean(childActivity);
      childActivity.addBean(anotherMyBean);
   }

   @Test(groups = { "broken" }, expectedExceptions = { InjectionException.class })
   public void testSameBeanTypeInChildAsIndirectParentInjection()
   {
      WebBeansManager childActivity = getCurrentManager().createActivity();
      WebBeansManager grandChildActivity = childActivity.createActivity();
      Bean<?> anotherMyBean = createDummyBean(grandChildActivity);
      grandChildActivity.addBean(anotherMyBean);
   }
}
