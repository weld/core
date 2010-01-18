package org.jboss.weld.tests.activities.current;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.tests.TestableObserverMethod;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 * 
 */
@Artifact
public class EventCurrentActivityTest extends AbstractWeldTest
{

   private static class DummyContext implements Context
   {

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
         return true;
      }

   }


   @Test(groups = "broken")
   public void testEventProcessedByCurrentActivity()
   {
      DummyContext dummyContext = new DummyContext();
      getCurrentManager().addContext(dummyContext);
      BeanManagerImpl childActivity = getCurrentManager().createActivity();
      TestableObserverMethod<NightTime> observer = new TestableObserverMethod<NightTime>()
      {

         boolean observed = false;

         public void notify(NightTime event)
         {
            observed = true;
         }

         public boolean isObserved()
         {
            return observed;
         }

         public Class<?> getBeanClass()
         {
            return NightTime.class;
         }

         public Set<Annotation> getObservedQualifiers()
         {
            return Collections.<Annotation>singleton(AnyLiteral.INSTANCE);
         }

         public Type getObservedType()
         {
            return NightTime.class;
         }

         public Reception getReception()
         {
            return Reception.ALWAYS;
         }

         public TransactionPhase getTransactionPhase()
         {
            return TransactionPhase.IN_PROGRESS;
         }
         
      };
      childActivity.addObserver(observer);
      childActivity.setCurrent(dummyContext.getScope());
      getReference(Dusk.class).ping();
      assert observer.isObserved();
   }
}
