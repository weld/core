package org.jboss.weld.test.unit.annotated.decoration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Artifact
public class AnnotatedTypeDecoratorTest extends AbstractWebBeansTest
{
   @Test
   public void testAnnotationDecorator() throws Exception 
   {
      NotAnnotated.reset();
      AnnotatedType<NotAnnotated> type = getCurrentManager().createAnnotatedType(NotAnnotated.class);
      checkAnnotations(type, new NoAnnotationsChecker());
      
      type = new MockAnnotatedType<NotAnnotated>(type);
      checkAnnotations(type, new MockAnnotationsChecker());
      
      NonContextual<NotAnnotated> nonContextual = new NonContextual<NotAnnotated>(getCurrentManager(), type);
      NotAnnotated instance = nonContextual.create();
      assertNotNull(instance);
      nonContextual.postConstruct(instance);
      
      assertNotNull(instance.getFromField());
      assertNotNull(NotAnnotated.getFromConstructor());
      assertNotNull(NotAnnotated.getFromInitializer());
   }
   
   private void checkAnnotations(AnnotatedType<NotAnnotated> type, TypeChecker checker)
   {
      checker.assertAnnotations(type);
      
      assertEquals(1, type.getConstructors().size());
      
      checker.assertAnnotations(type.getConstructors().iterator().next());
      checker.assertAnnotations(type.getConstructors().iterator().next().getParameters().get(0));
      
      assertEquals(3, type.getFields().size());
      for (AnnotatedField<? super NotAnnotated> field : type.getFields())
      {
         if (field.getJavaMember().getName().equals("fromField"))
         {
            checker.assertAnnotations(field);
         }
         else
         {
            assertEquals(0, field.getAnnotations().size());
         }
      }
      assertEquals(5, type.getMethods().size());
      checker.assertAnnotations(type.getMethods().iterator().next());
   }




   interface TypeChecker
   {
      void assertAnnotations(Annotated annotated);
   }
   
   class NoAnnotationsChecker implements TypeChecker
   {

      public void assertAnnotations(Annotated annotated)
      {
         assertEquals(0, annotated.getAnnotations().size());
      }
   }
   
   class MockAnnotationsChecker implements TypeChecker
   {

      public void assertAnnotations(Annotated annotated)
      {
         if (annotated instanceof MockAnnotatedCallable)
         {
            assertEquals(1, annotated.getAnnotations().size());
            assertTrue(annotated.isAnnotationPresent(Inject.class));
         }
         else if (annotated instanceof MockAnnotatedField<?>)
         {
            assertEquals(1, annotated.getAnnotations().size());
            assertTrue(annotated.isAnnotationPresent(Inject.class));
         }
      }
   }
   
   public class NonContextual<T> {

      final InjectionTarget<T> it;
      final BeanManager manager;
      CreationalContext<T> cc;

      public NonContextual(BeanManager manager, AnnotatedType<T> type) {
         this.manager = manager;
         this.it = manager.createInjectionTarget(type);
         cc = manager.createCreationalContext(null);
      }

      public T create()
      {
         return it.produce(cc);
      }
      
      public CreationalContext<T> postConstruct(T instance) {
         it.inject(instance, cc);
         it.postConstruct(instance);
         return cc;
      }

      public void preDestroy(T instance) {
         it.preDestroy(instance);
      }
   }
}
