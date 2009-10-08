package org.jboss.weld.test.unit.activities.current;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class NonNormalScopeTest extends AbstractWeldTest
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

   private static class NonNormalContext extends DummyContext
   {

      @Override
      public Class<? extends Annotation> getScope()
      {
         return NonNormalScope.class;
      }

   }

   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testNonNormalScope()
   {
      Context dummyContext = new NonNormalContext();
      getCurrentManager().addContext(dummyContext);
      WeldManager childActivity = getCurrentManager().createActivity();
      childActivity.setCurrent(dummyContext.getScope());
   }
}
