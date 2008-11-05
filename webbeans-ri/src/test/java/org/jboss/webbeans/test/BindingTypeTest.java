package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;
import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.beans.Antelope;
import org.jboss.webbeans.test.beans.Cat;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class BindingTypeTest extends AbstractTest 
{
	
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section={"2.3.3", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInJava()
   {
      SimpleBeanModel<Order> order = createSimpleModel(Order.class, manager);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }

   @Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="2.3.1")
   public void testDefaultBindingTypeAssumedAtInjectionPoint() throws Exception
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups="annotationDefinition") @SpecAssertion(section="2.3.2")
   public void testBindingTypeDeclaresBindingTypeAnnotation()
   {
      assert false;
   }
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.3")
   public void testBindingTypesDeclaredInJava()
   {
      SimpleBeanModel<Cat> cat = createSimpleModel(Cat.class, manager);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Synchronous.class);
   }
   
   @Test @SpecAssertion(section="2.3.3") 
   public void testMultipleBindingTypes()
   {
      BeanModel<?, ?> model = createSimpleModel(Cod.class, manager);
      assert model.getBindingTypes().size() == 2;
   }
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.4")
   public void testBindingTypesDeclaredInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedClass annotatedItem = new SimpleAnnotatedClass(Antelope.class, annotations);
      
      SimpleBeanModel<Antelope> antelope = new SimpleBeanModel<Antelope>(getEmptyAnnotatedType(Antelope.class), annotatedItem, manager);
      assert Reflections.annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
   }

   @Test @SpecAssertion(section="2.3.4")
   public void testXmlBindingTypeOverridesAndIgnoresJava()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedClass<Cat> annotatedItem = new SimpleAnnotatedClass<Cat>(Cat.class, annotations);
      
      SimpleBeanModel<Cat> cat = createSimpleModel(Cat.class, annotatedItem, manager);
      assert cat.getBindingTypes().size() == 1;
      assert cat.getBindingTypes().contains(new AnnotationLiteral<Asynchronous>() {});
   }
   
	@Test @SpecAssertion(section="2.3.4") 
   public void testNoBindingTypesDeclaredInXml()
   {
	   Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedClass<Cat> annotatedItem = new SimpleAnnotatedClass<Cat>(Cat.class, annotations);
      
      SimpleBeanModel<Cat> cat = createSimpleModel(Cat.class, annotatedItem, manager);
      assert cat.getBindingTypes().size() == 1;
      assert cat.getBindingTypes().contains(new AnnotationLiteral<Synchronous>() {});
   }
	
	@Test @SpecAssertion(section={"2.3.4", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInXml()
   {
      BeanModel<?, ?> model = createSimpleModel(Tuna.class, manager);
      assert model.getBindingTypes().size() == 1;
      assert model.getBindingTypes().contains(new CurrentAnnotationLiteral());
   }
	

	
	@Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="2.3.5") 
   public void testFieldInjectedFromProducerMethod()
   {
      assert false;
   }
	
	@Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXml()
   {
      assert false;
   }
	
	@Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXmlIgnoresAnnotations()
   {
      assert false;
   }
	
	@Test(groups={"injection", "producerMethod"})
   public void testMethodWithBindingAnnotationsOnParametersAreInjected()
   {
      assert false;
   }
	
	@Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXml()
   {
      assert false;
   }
	
	@Test(groups={"injection", "webbeansxml"}) @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXmlIgnoresAnnotations()
   {
      assert false;
   }

}
