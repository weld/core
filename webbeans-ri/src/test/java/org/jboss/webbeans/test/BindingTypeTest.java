package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Method;

import javax.webbeans.Current;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.beans.Barn;
import org.jboss.webbeans.test.beans.Cat;
import org.jboss.webbeans.test.beans.Cod;
import org.jboss.webbeans.test.beans.DefangedTarantula;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class BindingTypeTest extends AbstractTest 
{
	
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section={"2.3.3", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInJava()
   {
      SimpleBean<Order> order = createSimpleBean(Order.class, manager);
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectTarget()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.3.2")
   public void testBindingTypeHasCorrectRetention()
   {
      assert false;
   }

   @Test(groups={"stub", "annotationDefinition"}) @SpecAssertion(section="2.3.2")
   public void testBindingTypeDeclaresBindingTypeAnnotation()
   {
      assert false;
   }
   
   @SuppressWarnings("unchecked")
   @Test @SpecAssertion(section="2.3.3")
   public void testBindingTypesDeclaredInJava()
   {
      SimpleBean<Cat> cat = createSimpleBean(Cat.class, manager);
      assert cat.getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(cat.getBindingTypes(), Synchronous.class);
   }
   
   @Test @SpecAssertion(section="2.3.3") 
   public void testMultipleBindingTypes()
   {
      SimpleBean<?> model = createSimpleBean(Cod.class, manager);
      assert model.getBindingTypes().size() == 2;
   }
   
   @SuppressWarnings("unchecked")
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.3.4")
   public void testBindingTypesDeclaredInXml()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      //AnnotatedClass annotatedItem = new SimpleAnnotatedClass(Antelope.class, annotations);
      
      //SimpleBean<Antelope> antelope = createSimpleBean(Antelope.class, annotatedItem, manager);
      // assert Reflections.annotationSetMatches(antelope.getBindingTypes(), Asynchronous.class);
      assert false;
   }

   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.3.4")
   public void testXmlBindingTypeOverridesAndIgnoresJava()
   {
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      //AnnotatedClass<Cat> annotatedItem = new SimpleAnnotatedClass<Cat>(Cat.class, annotations);
      
      //SimpleBean<Cat> cat = createSimpleBean(Cat.class, annotatedItem, manager);
      //assert cat.getBindingTypes().size() == 1;
      //assert cat.getBindingTypes().contains(new AnnotationLiteral<Asynchronous>() {});
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="2.3.4")
   public void testNoBindingTypesDeclaredInXml()
   {
	   //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //AnnotatedClass<Cat> annotatedItem = new SimpleAnnotatedClass<Cat>(Cat.class, annotations);
      
      //SimpleBean<Cat> cat = createSimpleBean(Cat.class, annotatedItem, manager);
      //assert cat.getBindingTypes().size() == 1;
      //assert cat.getBindingTypes().contains(new AnnotationLiteral<Synchronous>() {});
      assert false;
   }
	
	@Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section={"2.3.4", "2.3.1"}) 
   public void testDefaultBindingTypeDeclaredInXml()
   {
      SimpleBean<?> model = createSimpleBean(Tuna.class, manager);
      assert model.getBindingTypes().size() == 1;
      assert model.getBindingTypes().contains(new CurrentAnnotationLiteral());
      assert false;
   }
	

	
	@Test(groups={"injection", "producerMethod"}) @SpecAssertion(section="2.3.5") 
   public void testFieldInjectedFromProducerMethod() throws Exception
   {
	   SimpleBean<SpiderProducer> spiderProducer = createSimpleBean(SpiderProducer.class, manager);
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
	   manager.addBean(createProducerMethodBean(Tarantula.class, method, manager, spiderProducer));
      Barn barn = createSimpleBean(Barn.class, manager).create();
      assert barn.petSpider != null;
      assert barn.petSpider instanceof DefangedTarantula;
   }
	
	@Test(groups={"stub", "injection", "webbeansxml"}) @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXml()
   {
      assert false;
   }
	
	@Test(groups={"stub", "injection", "webbeansxml"}) @SpecAssertion(section="2.3.5") 
   public void testFieldWithBindingTypeInXmlIgnoresAnnotations()
   {
      assert false;
   }
	
	@Test(groups={"injection", "producerMethod"})
   public void testMethodWithBindingAnnotationsOnParametersAreInjected() throws Exception
   {
	   SimpleBean<SpiderProducer> spiderProducer = createSimpleBean(SpiderProducer.class, manager);
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      manager.addBean(createProducerMethodBean(Tarantula.class, method, manager, spiderProducer));
      method = SpiderProducer.class.getMethod("produceSpiderFromInjection", Tarantula.class);
      ProducerMethodBean<Spider> spiderBean = createProducerMethodBean(Spider.class, method, manager, spiderProducer);
      Spider spider = spiderBean.create();
      assert spider != null;
      assert spider instanceof DefangedTarantula;
   }
	
	@Test(groups={"stub", "injection", "webbeansxml"}) @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXml()
   {
      assert false;
   }
	
	@Test(groups={"stub", "injection", "webbeansxml"}) @SpecAssertion(section="2.3.6") 
   public void testMethodWithBindingAnnotationsOnParametersDeclaredInXmlIgnoresAnnotations()
   {
      assert false;
   }

}
