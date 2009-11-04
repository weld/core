package org.jboss.weld.test.nonContextual;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.metadata.validation.ValidationException;
import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Artifact
public class ExampleTest extends AbstractWeldTest
{
   @Test
   public void testNonContextual() throws Exception 
   {
      NonContextual<External> nonContextual = new NonContextual<External>(getCurrentManager(), External.class);
      
      External external = new External();
      Assert.assertNull(external.bean);
      nonContextual.postConstruct(external);
      Assert.assertNotNull(external.bean);
      nonContextual.preDestroy(external);
      // preDestroy doesn't cause any dis-injection
      Assert.assertNotNull(external.bean);      
   }
   
   @Test
   public void validateNonContextual() throws Exception
   {
      NonContextual<External> nonContextual = new NonContextual<External>(getCurrentManager(), External.class);

      for (InjectionPoint point : nonContextual.it.getInjectionPoints())
      {
         try
         {
            getCurrentManager().validate(point);
         }
         catch(ValidationException e)
         {
            Assert.fail("Should have been valid");
         }
      }
   }
   
   
   public class NonContextual<T> {

      final InjectionTarget<T> it;
      final BeanManager manager;

      public NonContextual(BeanManager manager, Class<T> clazz) {
         this.manager = manager;
         AnnotatedType<T> type = manager.createAnnotatedType(clazz);
         this.it = manager.createInjectionTarget(type);
      }

      public CreationalContext<T> postConstruct(T instance) {
         CreationalContext<T> cc = manager.createCreationalContext(null);
         it.inject(instance, cc);
         it.postConstruct(instance);
         return cc;
      }

      public void preDestroy(T instance) {
         it.preDestroy(instance);
      }
   }

}
