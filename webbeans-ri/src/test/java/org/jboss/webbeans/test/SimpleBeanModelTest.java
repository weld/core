package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.bindings.DependentAnnotationLiteral;
import org.jboss.webbeans.bindings.NamedAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.beans.Chair;
import org.jboss.webbeans.test.beans.Cow;
import org.jboss.webbeans.test.beans.Goldfish;
import org.jboss.webbeans.test.beans.Gorilla;
import org.jboss.webbeans.test.beans.Horse;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.broken.Carp;
import org.jboss.webbeans.test.beans.broken.Pig;
import org.jboss.webbeans.test.beans.broken.OuterBean.InnerBean;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("20080925")
public class SimpleBeanModelTest extends AbstractTest
{   
   
   // **** TESTS FOR STEREOTYPES **** //
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.7.3")
   public void testStereotypeDeclaredInXmlAndJava()
   {
      Map<Class<? extends Annotation>, Annotation> orderXmlAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      orderXmlAnnotations.put(Current.class, new CurrentAnnotationLiteral());
      orderXmlAnnotations.put(Synchronous.class, new SynchronousAnnotationLiteral());
      orderXmlAnnotations.put(Named.class, new NamedAnnotationLiteral (){
         
         public String value()
         {
            return "currentSynchronousOrder";
         }
         
      });
      AnnotatedType currentSynchronousOrderAnnotatedItem = new SimpleAnnotatedType(Order.class, orderXmlAnnotations);
      
      SimpleBeanModel<Order> order = new SimpleBeanModel<Order>(new SimpleAnnotatedType(Order.class), currentSynchronousOrderAnnotatedItem, manager);
      assert Production.class.equals(order.getDeploymentType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      assert Reflections.annotationSetMatches(order.getBindingTypes(), Current.class, Synchronous.class);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testSingleStereotype()
   {
      SimpleBeanModel<Gorilla> gorilla = new SimpleBeanModel<Gorilla>(new SimpleAnnotatedType<Gorilla>(Gorilla.class), getEmptyAnnotatedType(Gorilla.class), manager);
      assert gorilla.getName() == null;
      assert gorilla.getDeploymentType().equals(Production.class);
      assert gorilla.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert gorilla.getScopeType().equals(RequestScoped.class);
   }
   
   @Test @SpecAssertion(section="2.7.4")
   public void testRequiredTypeIsImplemented()
   {
      try
      {
         new SimpleBeanModel<Gorilla>(new SimpleAnnotatedType<Gorilla>(Gorilla.class), getEmptyAnnotatedType(Gorilla.class), manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test(expectedExceptions=Exception.class) @SpecAssertion(section="2.7.4")
   public void testRequiredTypeIsNotImplemented()
   {
      new SimpleBeanModel<Chair>(new SimpleAnnotatedType<Chair>(Chair.class), getEmptyAnnotatedType(Chair.class), manager);      
   }
   
   @Test @SpecAssertion(section="2.7.4")
   public void testScopeIsSupported()
   {
      try
      {
         new SimpleBeanModel<Goldfish>(new SimpleAnnotatedType<Goldfish>(Goldfish.class), getEmptyAnnotatedType(Goldfish.class), manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test(expectedExceptions=Exception.class) @SpecAssertion(section="2.7.4")
   public void testScopeIsNotSupported()
   {
      new SimpleBeanModel<Carp>(new SimpleAnnotatedType<Carp>(Carp.class), getEmptyAnnotatedType(Carp.class), manager);    
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testMultipleStereotypes()
   {
	   assert false;
   }
   
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
