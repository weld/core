package org.jboss.webbeans.test;


import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.bindings.DependentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.HornedAnimalDeploymentType;
import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.HornedMamalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.Chair;
import org.jboss.webbeans.test.components.Cow;
import org.jboss.webbeans.test.components.Goldfish;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Haddock;
import org.jboss.webbeans.test.components.Horse;
import org.jboss.webbeans.test.components.Moose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.SeaBass;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.components.broken.Carp;
import org.jboss.webbeans.test.components.broken.ComponentWithTooManyDeploymentTypes;
import org.jboss.webbeans.test.components.broken.Pig;
import org.jboss.webbeans.test.components.broken.OuterComponent.InnerComponent;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("20080925")
public class SimpleComponentModelTest extends AbstractTest
{
   
   private abstract class NamedAnnotationLiteral extends AnnotationLiteral<Named> implements Named {}
   
   // **** TESTS FOR DEPLOYMENT TYPE **** //
   
   @Test @SpecAssertion(section="2.5.3")
   public void testTooManyDeploymentTypes()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<ComponentWithTooManyDeploymentTypes>(new SimpleAnnotatedType<ComponentWithTooManyDeploymentTypes>(ComponentWithTooManyDeploymentTypes.class), getEmptyAnnotatedItem(ComponentWithTooManyDeploymentTypes.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlDeploymentTypeOverridesJava()
   {
      Map<Class<? extends Annotation>, Annotation> xmlDefinedDeploymentTypeAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      xmlDefinedDeploymentTypeAnnotations.put(AnotherDeploymentType.class, new AnotherDeploymentTypeAnnotationLiteral());
      AnnotatedType<ComponentWithTooManyDeploymentTypes> xmlDefinedDeploymentTypeAnnotatedItem = new SimpleAnnotatedType<ComponentWithTooManyDeploymentTypes>(ComponentWithTooManyDeploymentTypes.class, xmlDefinedDeploymentTypeAnnotations);
      
      SimpleComponentModel<ComponentWithTooManyDeploymentTypes> component = new SimpleComponentModel<ComponentWithTooManyDeploymentTypes>(new SimpleAnnotatedType<ComponentWithTooManyDeploymentTypes>(ComponentWithTooManyDeploymentTypes.class), xmlDefinedDeploymentTypeAnnotatedItem, manager);
      assert component.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test @SpecAssertion(section="2.5.5")
   public void testXmlDefaultDeploymentType()
   {
      AnnotatedType<Antelope> antelopeAnnotatedItem = new SimpleAnnotatedType<Antelope>(Antelope.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleComponentModel<Antelope> antelope = new SimpleComponentModel<Antelope>(getEmptyAnnotatedItem(Antelope.class), antelopeAnnotatedItem, manager);
      assert antelope.getDeploymentType().equals(Production.class);
   }
   
   @Test @SpecAssertion(section="2.5.4")
   public void testXmlRespectsJavaDeploymentType()
   {
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, new HashMap<Class<? extends Annotation>, Annotation>());
      SimpleComponentModel<Tuna> tuna = new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      assert tuna.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test @SpecAssertion(section="2.5.7")
   public void testDeploymentTypePrecedenceSelection()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(HornedMammalStereotype.class, new HornedMamalStereotypeAnnotationLiteral());
      AnnotatedType<Moose> annotatedItem = new SimpleAnnotatedType<Moose>(Moose.class, annotations);
      
      SimpleComponentModel<Moose> moose = new SimpleComponentModel<Moose>(new SimpleAnnotatedType<Moose>(Moose.class), annotatedItem, manager);
      assert moose.getDeploymentType().equals(HornedAnimalDeploymentType.class);
      
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testDeploymentTypeSpecifiedAndStereotyped()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(FishStereotype.class, new FishStereotypeAnnotationLiteral());
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      assert trout.getScopeType().equals(RequestScoped.class);
   } 
   
   // **** TESTS FOR SCOPES **** //
   

   
   // **** TESTS FOR COMPONENT NAME **** /
   
   @Test @SpecAssertion(section="2.6.1")
   public void testDefaultNamed()
   {
      SimpleComponentModel<Haddock> haddock = new SimpleComponentModel<Haddock>(new SimpleAnnotatedType<Haddock>(Haddock.class), getEmptyAnnotatedItem(Haddock.class), manager);
      assert haddock.getName() != null;
      assert haddock.getName().equals("haddock");
   }
   
   @Test @SpecAssertion(section="2.6.2")
   public void testDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral() {
         
         public String value()
         {
            return "";
         }
         
      });
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      
      assert trout.getName() != null;
      assert trout.getName().equals("seaBass");
   }
   
   @Test @SpecAssertion(section="2.6.2")
   public void testNonDefaultXmlNamed()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Named.class, new NamedAnnotationLiteral(){
         
         public String value()
         {
            return "aTrout";
         }
         
      });
      AnnotatedType<SeaBass> annotatedItem = new SimpleAnnotatedType<SeaBass>(SeaBass.class, annotations);
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), annotatedItem, manager);
      
      assert trout.getName().equals("aTrout");
   }
   
   @Test @SpecAssertion(section="2.6.4")
   public void testNotNamed()
   {
      SimpleComponentModel<SeaBass> trout = new SimpleComponentModel<SeaBass>(new SimpleAnnotatedType<SeaBass>(SeaBass.class), getEmptyAnnotatedItem(SeaBass.class), manager);
      assert trout.getName() == null;
   }
   
   @Test @SpecAssertion(section="2.6.1")
   public void testNonDefaultNamed()
   {
      SimpleComponentModel<Moose> moose = new SimpleComponentModel<Moose>(new SimpleAnnotatedType<Moose>(Moose.class), getEmptyAnnotatedItem(Moose.class), manager);
      assert moose.getName().equals("aMoose");
   }
   
   
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
      
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), currentSynchronousOrderAnnotatedItem, manager);
      assert Production.class.equals(order.getDeploymentType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      assert Reflections.annotationSetMatches(order.getBindingTypes(), Current.class, Synchronous.class);
      assert order.getScopeType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testSingleStereotype()
   {
      SimpleComponentModel<Gorilla> gorilla = new SimpleComponentModel<Gorilla>(new SimpleAnnotatedType<Gorilla>(Gorilla.class), getEmptyAnnotatedItem(Gorilla.class), manager);
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
         new SimpleComponentModel<Gorilla>(new SimpleAnnotatedType<Gorilla>(Gorilla.class), getEmptyAnnotatedItem(Gorilla.class), manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test(expectedExceptions=Exception.class) @SpecAssertion(section="2.7.4")
   public void testRequiredTypeIsNotImplemented()
   {
      new SimpleComponentModel<Chair>(new SimpleAnnotatedType<Chair>(Chair.class), getEmptyAnnotatedItem(Chair.class), manager);      
   }
   
   @Test @SpecAssertion(section="2.7.4")
   public void testScopeIsSupported()
   {
      try
      {
         new SimpleComponentModel<Goldfish>(new SimpleAnnotatedType<Goldfish>(Goldfish.class), getEmptyAnnotatedItem(Goldfish.class), manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
      
   }
   
   @Test(expectedExceptions=Exception.class) @SpecAssertion(section="2.7.4")
   public void testScopeIsNotSupported()
   {
      new SimpleComponentModel<Carp>(new SimpleAnnotatedType<Carp>(Carp.class), getEmptyAnnotatedItem(Carp.class), manager);    
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testMultipleStereotypes()
   {
	   assert false;
   }
   
   //*** COMPONENT CLASS CHECKS ****//
   
   @Test
   public void testAbstractClassIsNotAllowed()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Cow>(new SimpleAnnotatedType<Cow>(Cow.class), getEmptyAnnotatedItem(Cow.class), manager);
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
         new SimpleComponentModel<InnerComponent>(new SimpleAnnotatedType<InnerComponent>(InnerComponent.class), getEmptyAnnotatedItem(InnerComponent.class), manager);
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
         new SimpleComponentModel<Horse>(new SimpleAnnotatedType<Horse>(Horse.class), getEmptyAnnotatedItem(Horse.class), manager);
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
         new SimpleComponentModel<Horse>(new SimpleAnnotatedType<Horse>(Horse.class), annotatedItem, manager);
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
         new SimpleComponentModel<Pig>(new SimpleAnnotatedType<Pig>(Pig.class), getEmptyAnnotatedItem(Pig.class), manager);
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
         new SimpleComponentModel<Pig>(new SimpleAnnotatedType<Pig>(Pig.class), annotatedItem, manager);
      }
      catch (Exception e) 
      {
         assert false;
      }
   }
   
   
}
