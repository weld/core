package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createProducerModel;
import static org.jboss.webbeans.test.util.Util.createSimpleModel;

import java.lang.reflect.Method;

import javax.webbeans.Current;
import javax.webbeans.DefinitionException;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.model.bean.ProducerMethodBeanModel;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.BlackWidow;
import org.jboss.webbeans.test.beans.DaddyLongLegs;
import org.jboss.webbeans.test.beans.DeadlyAnimal;
import org.jboss.webbeans.test.beans.DeadlySpider;
import org.jboss.webbeans.test.beans.LadybirdSpider;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.WolfSpider;
import org.jboss.webbeans.test.beans.broken.BeanWithStaticProducerMethod;
import org.jboss.webbeans.test.beans.broken.BrokenSpiderProducer;
import org.testng.annotations.Test;

public class ProducerMethodBeanModelTest extends AbstractTest
{
   
   @Test(groups="producerMethod") @SpecAssertion(section="2.5.3")
   public void testProducerMethodInheritsDeploymentTypeOfDeclaringWebBean() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = createProducerModel(Tarantula.class, method, manager);
      tarantulaModel.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testStaticMethod() throws Exception
   {
      SimpleBeanModel<BeanWithStaticProducerMethod> model = createSimpleModel(BeanWithStaticProducerMethod.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = BeanWithStaticProducerMethod.class.getMethod("getString");
      createProducerModel(String.class, method, manager);
   }
   
   @Test(groups={"producerMethod", "enterpiseBeans"}, expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
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
   
   @Test(groups={"producerMethod", "deployment"}) @SpecAssertion(section="3.4")
   public void testBeanDeclaresMultipleProducerMethods()
   {
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4")
   public void testDefaultBindingType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = createProducerModel(Tarantula.class, method, manager);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForClassReturn() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = createProducerModel(Tarantula.class, method, manager);
      assert tarantulaModel.getApiTypes().size() == 6;
      assert tarantulaModel.getApiTypes().contains(Tarantula.class);
      assert tarantulaModel.getApiTypes().contains(DeadlySpider.class);
      assert tarantulaModel.getApiTypes().contains(Spider.class);
      assert tarantulaModel.getApiTypes().contains(Animal.class);
      assert tarantulaModel.getApiTypes().contains(DeadlyAnimal.class);
      assert tarantulaModel.getApiTypes().contains(Object.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForInterfaceReturn() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("makeASpider");
      ProducerMethodBeanModel<Animal> animalModel = createProducerModel(Animal.class, method, manager);
      assert animalModel.getApiTypes().size() == 2;
      assert animalModel.getApiTypes().contains(Animal.class);
      assert animalModel.getApiTypes().contains(Object.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForPrimitiveReturn() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("getWolfSpiderSize");
      ProducerMethodBeanModel<Integer> intModel = createProducerModel(int.class, method, manager);
      assert intModel.getApiTypes().size() == 2;
      assert intModel.getApiTypes().contains(int.class);
      assert intModel.getApiTypes().contains(Object.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.1")
   public void testApiTypeForArrayTypeReturn() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("getSpiders");
      ProducerMethodBeanModel<Spider[]> spidersModel = createProducerModel(Spider[].class, method, manager);
      assert spidersModel.getApiTypes().size() == 2;
      assert spidersModel.getApiTypes().contains(Spider[].class);
      assert spidersModel.getApiTypes().contains(Object.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testBindingType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceTameTarantula");
      ProducerMethodBeanModel<Tarantula> tarantulaModel = createProducerModel(Tarantula.class, method, manager);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testScopeType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceDaddyLongLegs");
      ProducerMethodBeanModel<DaddyLongLegs> daddyLongLegsModel = createProducerModel(DaddyLongLegs.class, method, manager);
      assert daddyLongLegsModel.getScopeType().equals(RequestScoped.class);
      
      // TODO Inherit scope from returned web bean?
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testDeploymentType() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("getLadybirdSpider");
      ProducerMethodBeanModel<LadybirdSpider> ladybirdSpiderModel = createProducerModel(LadybirdSpider.class, method, manager);
      assert ladybirdSpiderModel.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testNamedMethod() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceBlackWidow");
      ProducerMethodBeanModel<BlackWidow> blackWidowSpiderModel = createProducerModel(BlackWidow.class, method, manager);
      assert blackWidowSpiderModel.getName().equals("blackWidow");
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.4.2")
   public void testDefaultNamedMethod() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceDaddyLongLegs");
      ProducerMethodBeanModel<DaddyLongLegs> daddyLongLegsSpiderModel = createProducerModel(DaddyLongLegs.class, method, manager);
      assert daddyLongLegsSpiderModel.getName().equals("produceDaddyLongLegs");
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodAnnotatedDestructor() throws Exception
   {
      SimpleBeanModel<BrokenSpiderProducer> model = createSimpleModel(BrokenSpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = BrokenSpiderProducer.class.getMethod("destroy");
      createProducerModel(String.class, method, manager);
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodWithParameterAnnotatedDisposes() throws Exception
   {
      SimpleBeanModel<BrokenSpiderProducer> model = createSimpleModel(BrokenSpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = BrokenSpiderProducer.class.getMethod("dispose", String.class);
      createProducerModel(String.class, method, manager);
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerMethodWithParameterAnnotatedObserves() throws Exception
   {
      SimpleBeanModel<BrokenSpiderProducer> model = createSimpleModel(BrokenSpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = BrokenSpiderProducer.class.getMethod("observe", String.class);
      createProducerModel(String.class, method, manager);
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
   public void testStereotype() throws Exception
   {
      SimpleBeanModel<SpiderProducer> model = createSimpleModel(SpiderProducer.class, manager);
      manager.getModelManager().addBeanModel(model);
      Method method = SpiderProducer.class.getMethod("produceWolfSpider");
      ProducerMethodBeanModel<WolfSpider> wolfSpiderModel = createProducerModel(WolfSpider.class, method, manager);
      assert wolfSpiderModel.getMergedStereotypes().getRequiredTypes().size() == 1;
      assert wolfSpiderModel.getMergedStereotypes().getRequiredTypes().contains(Animal.class);
      assert wolfSpiderModel.getScopeType().equals(RequestScoped.class);
   }
}
