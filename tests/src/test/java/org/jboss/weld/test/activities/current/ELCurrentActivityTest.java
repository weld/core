package org.jboss.weld.test.activities.current;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class ELCurrentActivityTest extends AbstractWeldTest
{


   private static class DummyContext implements Context
   {

      private boolean active = true;

      public <T> T get(Contextual<T> contextual)
      {
         return null;
      }

      public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
      {
         return null;
      }

      public Class<? extends Annotation> getScope()
      {
         return Dummy.class;
      }

      public boolean isActive()
      {
         return active;
      }

      public void setActive(boolean active)
      {
         this.active = active;
      }

   }

   private static class Daisy implements Bean<Cow>
   {

      private static final Set<Type> TYPES = new HashSet<Type>();

      private final static Set<Annotation> BINDING_TYPES = new HashSet<Annotation>();


      static
      {
         TYPES.add(Cow.class);
         TYPES.add(Object.class);
         BINDING_TYPES.add(new AnnotationLiteral<Tame>() {});
      }

      public Daisy(BeanManager beanManager)
      {
      }

      public Set<Annotation> getQualifiers()
      {
         return BINDING_TYPES;
      }

      public Set<InjectionPoint> getInjectionPoints()
      {
         return Collections.emptySet();
      }

      public String getName()
      {
         return "daisy";
      }

      public Class<? extends Annotation> getScope()
      {
         return Dependent.class;
      }

      public Set<Type> getTypes()
      {
         return TYPES;
      }

      public boolean isNullable()
      {
         return true;
      }

      public boolean isSerializable()
      {
         return false;
      }

      public Cow create(CreationalContext<Cow> creationalContext)
      {
         return new Cow();
      }

      public void destroy(Cow instance, CreationalContext<Cow> creationalContext)
      {
         // TODO Auto-generated method stub

      }

      public Class<?> getBeanClass()
      {
         return Cow.class;
      }

      public boolean isAlternative()
      {
         return false;
      }

      public Set<Class<? extends Annotation>> getStereotypes()
      {
         return Collections.emptySet();
      }

   }

   @Test
   public void testELEvaluationProcessedByCurrentActivty()
   {
      Context dummyContext = new DummyContext();
      getCurrentManager().addContext(dummyContext);
      assert getBeans(Cow.class).size() == 1;
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
      childActivity.addBean(new Daisy(childActivity));
      childActivity.setCurrent(dummyContext.getScope());
      assert evaluateValueExpression("#{daisy}", Cow.class) != null;
   }

}
