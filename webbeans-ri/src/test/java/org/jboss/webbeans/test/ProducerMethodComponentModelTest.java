package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.webbeans.Current;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedMethod;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.ProducerMethodComponentModel;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.BlackWidow;
import org.jboss.webbeans.test.components.DaddyLongLegs;
import org.jboss.webbeans.test.components.DeadlyAnimal;
import org.jboss.webbeans.test.components.DeadlySpider;
import org.jboss.webbeans.test.components.LadybirdSpider;
import org.jboss.webbeans.test.components.Spider;
import org.jboss.webbeans.test.components.SpiderProducer;
import org.jboss.webbeans.test.components.Tarantula;
import org.jboss.webbeans.test.components.TrapdoorSpider;
import org.jboss.webbeans.test.components.broken.ComponentWithFinalProducerMethod;
import org.jboss.webbeans.test.components.broken.ComponentWithStaticProducerMethod;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProducerMethodComponentModelTest
{

   private ContainerImpl container;
   private AnnotatedType<?> emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
   }
   
   @Test @SpecAssertion(section="3.3")
   public void testStaticMethod() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<ComponentWithStaticProducerMethod> componentModel = new SimpleComponentModel<ComponentWithStaticProducerMethod>(new SimpleAnnotatedType<ComponentWithStaticProducerMethod>(ComponentWithStaticProducerMethod.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = ComponentWithStaticProducerMethod.class.getMethod("getString");
      boolean exception = false;
      try
      {
         new ProducerMethodComponentModel<String>(new SimpleAnnotatedMethod(method), container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test @SpecAssertion(section="3.3")
   public void testApiTypes() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodComponentModel<Tarantula> tarantulaModel = new ProducerMethodComponentModel<Tarantula>(new SimpleAnnotatedMethod(method), container);
      assert tarantulaModel.getApiTypes().contains(Tarantula.class);
      assert tarantulaModel.getApiTypes().contains(DeadlySpider.class);
      assert tarantulaModel.getApiTypes().contains(Spider.class);
      assert tarantulaModel.getApiTypes().contains(Animal.class);
      assert tarantulaModel.getApiTypes().contains(DeadlyAnimal.class);
      assert !tarantulaModel.getApiTypes().contains(Object.class);
   }
   
   @Test @SpecAssertion(section="3.3.1")
   public void testDefaultBindingType() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodComponentModel<Tarantula> tarantulaModel = new ProducerMethodComponentModel<Tarantula>(new SimpleAnnotatedMethod(method), container);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   @Test
   public void testBindingType() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      ProducerMethodComponentModel<Tarantula> tarantulaModel = new ProducerMethodComponentModel<Tarantula>(new SimpleAnnotatedMethod(method), container);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
   @Test @SpecAssertion(section="3.3")
   public void testFinalMethod() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<ComponentWithFinalProducerMethod> componentModel = new SimpleComponentModel<ComponentWithFinalProducerMethod>(new SimpleAnnotatedType<ComponentWithFinalProducerMethod>(ComponentWithFinalProducerMethod.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = ComponentWithFinalProducerMethod.class.getMethod("getString");
      boolean exception = false;
      try
      {
         new ProducerMethodComponentModel<String>(new SimpleAnnotatedMethod(method), container);     
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test @SpecAssertion(section="3.3")
   public void testFinalMethodWithDependentScope() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceTrapdoorSpider");
      ProducerMethodComponentModel<TrapdoorSpider> trapdoorSpiderModel = new ProducerMethodComponentModel<TrapdoorSpider>(new SimpleAnnotatedMethod(method), container);
      assert trapdoorSpiderModel.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test @SpecAssertion(section="3.3.6")
   public void testNamedMethod() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceBlackWidow");
      ProducerMethodComponentModel<BlackWidow> blackWidowSpiderModel = new ProducerMethodComponentModel<BlackWidow>(new SimpleAnnotatedMethod(method), container);
      assert blackWidowSpiderModel.getName().equals("blackWidow");
   }
   
   @Test @SpecAssertion(section="3.3.6")
   public void testDefaultNamedMethod() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("produceDaddyLongLegs");
      ProducerMethodComponentModel<DaddyLongLegs> daddyLongLegsSpiderModel = new ProducerMethodComponentModel<DaddyLongLegs>(new SimpleAnnotatedMethod(method), container);
      assert daddyLongLegsSpiderModel.getName().equals("produceDaddyLongLegs");
   }
   
   @Test @SpecAssertion(section="3.3.6")
   public void testDefaultNamedJavaBeanMethod() throws SecurityException, NoSuchMethodException
   {
      SimpleComponentModel<SpiderProducer> componentModel = new SimpleComponentModel<SpiderProducer>(new SimpleAnnotatedType<SpiderProducer>(SpiderProducer.class), emptyAnnotatedItem, container);
      container.getModelManager().addComponentModel(componentModel);
      Method method = SpiderProducer.class.getMethod("getLadybirdSpider");
      ProducerMethodComponentModel<LadybirdSpider> ladybirdSpiderModel = new ProducerMethodComponentModel<LadybirdSpider>(new SimpleAnnotatedMethod(method), container);
      assert ladybirdSpiderModel.getName().equals("ladybirdSpider");
   }
   
}
