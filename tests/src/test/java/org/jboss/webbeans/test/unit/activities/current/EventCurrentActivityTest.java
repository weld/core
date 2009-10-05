package org.jboss.webbeans.test.unit.activities.current;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class EventCurrentActivityTest extends AbstractWebBeansTest
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

   @Test
   public void testEventProcessedByCurrentActivity()
   {
//      Context dummyContext = new DummyContext();
//      getCurrentManager().addContext(dummyContext);
//      WebBeansManager childActivity = getCurrentManager().createActivity();
//      TestableObserver<NightTime> observer = new TestableObserver<NightTime>()
//      {
//
//         boolean observed = false;
//
//         public boolean notify(NightTime event)
//         {
//            observed = true;
//            return false;
//         }
//
//         public boolean isObserved()
//         {
//            return observed;
//         }

//      };
      //TODO Fix this test to use observer method within a child activity
//      childActivity.addObserver(observer);
//      childActivity.setCurrent(dummyContext.getScopeType());
//      createContextualInstance(Dusk.class).ping();
//      assert observer.isObserved();
   }

}
