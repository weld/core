package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Dependent;

import org.jboss.webbeans.bindings.DependentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.beans.Cow;
import org.jboss.webbeans.test.beans.Horse;
import org.jboss.webbeans.test.beans.broken.Pig;
import org.jboss.webbeans.test.beans.broken.OuterBean.InnerBean;
import org.testng.annotations.Test;

@SpecVersion("20080925")
public class SimpleBeanModelTest extends AbstractTest
{   
   
   // **** TESTS FOR STEREOTYPES **** //
   
   
   
   //*** BEAN CLASS CHECKS ****//
   
   @Test
   public void testAbstractClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new SimpleBeanModel<Cow>(new SimpleAnnotatedType<Cow>(Cow.class), getEmptyAnnotatedType(Cow.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testInnerClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new SimpleBeanModel<InnerBean>(new SimpleAnnotatedType<InnerBean>(InnerBean.class), getEmptyAnnotatedType(InnerBean.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testFinalClassMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new SimpleBeanModel<Horse>(new SimpleAnnotatedType<Horse>(Horse.class), getEmptyAnnotatedType(Horse.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentAnnotationLiteral());
      AnnotatedType<Horse> annotatedItem = new SimpleAnnotatedType<Horse>(Horse.class, annotations);
      try
      {
         new SimpleBeanModel<Horse>(new SimpleAnnotatedType<Horse>(Horse.class), annotatedItem, manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   @Test
   public void testClassWithFinalMethodMustBeDependentScoped()
   {
      boolean exception = false;
      try
      {
         new SimpleBeanModel<Pig>(new SimpleAnnotatedType<Pig>(Pig.class), getEmptyAnnotatedType(Pig.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Dependent.class, new DependentAnnotationLiteral());
      AnnotatedType<Pig> annotatedItem = new SimpleAnnotatedType<Pig>(Pig.class, annotations);
      try
      {
         new SimpleBeanModel<Pig>(new SimpleAnnotatedType<Pig>(Pig.class), annotatedItem, manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   
}
