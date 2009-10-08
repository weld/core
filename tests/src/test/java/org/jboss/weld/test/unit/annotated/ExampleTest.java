package org.jboss.weld.test.unit.annotated;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Artifact
public class ExampleTest extends AbstractWebBeansTest
{
   @Test
   public void testAnnotatedCallableGetParameters() throws Exception 
   {
      AnnotatedType<Bean> type = getCurrentManager().createAnnotatedType(Bean.class);
      
      assertNoAnnotations(type);
      
      Assert.assertEquals(1, type.getConstructors().size());
      for (AnnotatedConstructor<Bean> ctor : type.getConstructors())
      {
         assertNoAnnotations(ctor);
         
         for (AnnotatedParameter<Bean> param : ctor.getParameters())
         {
            assertNoAnnotations(param);
         }
      }
      
      Assert.assertEquals(1, type.getMethods().size());
      for (AnnotatedMethod<? super Bean> method : type.getMethods())
      {
         assertNoAnnotations(method);
         
         for (AnnotatedParameter<? super Bean> param : method.getParameters())
         {
            assertNoAnnotations(param);
         }
      }
      
      Assert.assertEquals(1, type.getFields().size());
      for (AnnotatedField<? super Bean> field : type.getFields())
      {
         assertNoAnnotations(field);
      }
   }

   private void assertNoAnnotations(Annotated annotated)
   {
      Assert.assertEquals(0, annotated.getAnnotations().size());
   }
}
