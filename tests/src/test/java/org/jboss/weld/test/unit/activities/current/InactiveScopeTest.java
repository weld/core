package org.jboss.weld.test.unit.activities.current;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.manager.api.WebBeansManager;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * Spec version: 20090519
 *
 */
@Artifact
public class InactiveScopeTest extends AbstractWebBeansTest
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

   @Test(expectedExceptions=ContextNotActiveException.class)
   public void testInactiveScope()
   {
      DummyContext dummyContext = new DummyContext();
      dummyContext.setActive(false);
      getCurrentManager().addContext(dummyContext);
      WebBeansManager childActivity = getCurrentManager().createActivity();
      childActivity.setCurrent(dummyContext.getScope());
   }

}
