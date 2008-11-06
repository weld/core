package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.reflect.Method;

import javax.webbeans.Current;
import javax.webbeans.DefinitionException;

import org.jboss.webbeans.introspector.impl.SimpleAnnotatedClass;
import org.jboss.webbeans.introspector.impl.SimpleAnnotatedMethod;
import org.jboss.webbeans.model.bean.ProducerMethodBeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.BlackWidow;
import org.jboss.webbeans.test.beans.DaddyLongLegs;
import org.jboss.webbeans.test.beans.DeadlyAnimal;
import org.jboss.webbeans.test.beans.DeadlySpider;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.broken.BeanWithStaticProducerMethod;
import org.testng.annotations.Test;

public class ProducerMethodBeanModelTest extends AbstractTest
{
   
   @Test(groups="producerMethod") @SpecAssertion(section="2.5.3")
   public void testProducerMethodInheritsDeploymentTypeOfDeclaringWebBean() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = new ProducerMethodBeanModel<Tarantula>(new SimpleAnnotatedMethod<Tarantula>(method), manager);
      tarantulaModel.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testStaticMethod() throws Exception
   {
      SimpleBeanModel<BeanWithStaticProducerMethod> model = new SimpleBeanModel<BeanWithStaticProducerMethod>(new SimpleAnnotatedClass<BeanWithStaticProducerMethod>(BeanWithStaticProducerMethod.class), getEmptyAnnotatedType(BeanWithStaticProducerMethod.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = BeanWithStaticProducerMethod.class.getMethod("getString");
      new ProducerMethodBeanModel<String>(new SimpleAnnotatedMethod<String>(method), manager);
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodIsNotBusinessMethod() throws Exception
   {
      assert false;
   }
   
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4")
   public void testProducerMethodReturnsNullIsDependent()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodReturnsNullIsNotDependent()
   {
      
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4")
   public void testParameterizedReturnType()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testParameterizedReturnTypeWithWildcard()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testParameterizedReturnTypeWithTypeParameter()
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4")
   public void testBeanDeclaresMultipleProducerMethods()
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4")
   public void testDefaultBindingType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = new ProducerMethodBeanModel<Tarantula>(new SimpleAnnotatedMethod<Tarantula>(method), manager);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForClassReturn() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = new ProducerMethodBeanModel<Tarantula>(new SimpleAnnotatedMethod<Tarantula>(method), manager);
      assert tarantulaModel.getApiTypes().contains(Tarantula.class);
      assert tarantulaModel.getApiTypes().contains(DeadlySpider.class);
      assert tarantulaModel.getApiTypes().contains(Spider.class);
      assert tarantulaModel.getApiTypes().contains(Animal.class);
      assert tarantulaModel.getApiTypes().contains(DeadlyAnimal.class);
      assert !tarantulaModel.getApiTypes().contains(Object.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForPrimitiveReturn() throws Exception
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForArrayTypeReturn() throws Exception
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testBindingType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = new ProducerMethodBeanModel<Tarantula>(new SimpleAnnotatedMethod<Tarantula>(method), manager);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testScopeType() throws Exception
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testDeploymentType() throws Exception
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testNamedMethod() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceBlackWidow");
      ProducerMethodBeanModel<BlackWidow> blackWidowSpiderModel = new ProducerMethodBeanModel<BlackWidow>(new SimpleAnnotatedMethod<BlackWidow>(method), manager);
      assert blackWidowSpiderModel.getName().equals("blackWidow");
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testDefaultNamedMethod() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = new SimpleBeanModel<SpiderProducer>(new SimpleAnnotatedClass<SpiderProducer>(SpiderProducer.class), getEmptyAnnotatedType(SpiderProducer.class), manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceDaddyLongLegs");
      ProducerMethodBeanModel<DaddyLongLegs> daddyLongLegsSpiderModel = new ProducerMethodBeanModel<DaddyLongLegs>(new SimpleAnnotatedMethod<DaddyLongLegs>(method), manager);
      assert daddyLongLegsSpiderModel.getName().equals("produceDaddyLongLegs");
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodAnnotatedObserver()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodWithParameterAnnotatedDisposes()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodWithParameterAnnotatedObserves()
   {
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testDisposalMethodNonStatic()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testDisposalMethodMethodDeclaredOnWebBeanImplementationClass()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testDisposalMethodBindingAnnotations()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testDisposalMethodDefaultBindingAnnotations()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testDisposalMethodDoesNotResolveToProducerMethod()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.5")
   public void testDisposalMethodDeclaredOnEnabledBean()
   {
      // TODO Placeholder
      // TODO Move this
      
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.4")
   public void testBeanCanDeclareMultipleDisposalMethods()
   {
      // TODO move this 
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.3.5")
   public void testProducerMethodHasNoMoreThanOneDisposalMethod()
   {
      // TODO move this 
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section={"2.7.2", "3.4.2"})
   public void testSingleStereotype()
   {
	   assert false;
   }
}
