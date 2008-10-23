package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Current;

import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.Cat;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

public class BindingTypeTest extends AbstractTest 
{
	
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section={"2.3.3", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInJava()
   {
      SimpleComponentModel<Order> order = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), emptyAnnotatedItem, manager);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }

   @Test(groups="injection") @SpecAssertion(section="2.3.1")
   public void testDefaultBindingTypeAssumedAtInjectionPoint()
   {
      assert false;
   }

   @Test @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test @SpecAssertion(section="2.3.2")
   public void testBindingTypeDeclaresBindingTypeAnnotation()
   {
      assert false;
   }
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.3")
   public void testBindingTypesDeclaredInJava()
   {
      SimpleComponentModel<Cat> cat = new SimpleComponentModel<Cat>(new SimpleAnnotatedType(Cat.class), emptyAnnotatedItem, manager);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Synchronous.class);
   }
   
   @Test @SpecAssertion(section="2.3.3") 
   public void testMultipleBindingTypes()
   {
      assert false;
   }
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.4")
   public void testBindingTypesDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Antelope.class, annotations);
      
      SimpleComponentModel<Antelope> antelope = new SimpleComponentModel<Antelope>(emptyAnnotatedItem, annotatedItem, manager);
      assert Reflections.annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
   }

	@SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.4")
   public void testXmlBindingTypeOverridesAndIgnoresJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Cat.class, annotations);
      
      SimpleComponentModel<Cat> cat = new SimpleComponentModel<Cat>(new SimpleAnnotatedType(Cat.class), annotatedItem, manager);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Asynchronous.class);
   }
   
	@Test @SpecAssertion(section="2.3.4") 
   public void testNoBindingTypesDeclaredInXml()
   {
      assert false;
   }
	
	@Test @SpecAssertion(section={"2.3.4", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInXml()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testFieldsWithBindingAnnotationsAreInjected()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testFieldMissingBindingTypeIsNotInjected()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testFieldInjectedFromProducerMethod()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXml()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXmlIgnoresAnnotations()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersAreInjected()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXml()
   {
      assert false;
   }
	
	@Test(groups="injection") @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXmlIgnoresAnnotations()
   {
      assert false;
   }

}
