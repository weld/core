package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createProducerFieldBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Field;

import javax.webbeans.Current;
import javax.webbeans.DefinitionException;
import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.BlackWidow;
import org.jboss.webbeans.test.beans.DaddyLongLegs;
import org.jboss.webbeans.test.beans.DeadlyAnimal;
import org.jboss.webbeans.test.beans.DeadlySpider;
import org.jboss.webbeans.test.beans.FunnelWeaver;
import org.jboss.webbeans.test.beans.LadybirdSpider;
import org.jboss.webbeans.test.beans.OtherSpiderProducer;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.WolfSpider;
import org.jboss.webbeans.test.beans.broken.OtherBrokenSpiderProducer;
import org.testng.annotations.Test;

public class ProducerFieldBeanModelTest extends AbstractTest
{
   
   @Test(groups="producerField") @SpecAssertion(section="2.5.3")
   public void testProducerFieldInheritsDeploymentTypeOfDeclaringWebBean() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceTameTarantula");
      ProducerFieldBean<Tarantula> tarantulaModel = createProducerFieldBean(Tarantula.class, field, bean);
      tarantulaModel.getDeploymentType().equals(AnotherDeploymentType.class);
   }
   
   /*@Test(groups="producerField", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testStaticField() throws Exception
   {
      SimpleBean<BeanWithStaticProducerField> bean = createSimpleBean(BeanWithStaticProducerField.class);
      manager.addBean(bean);
      Field field = BeanWithStaticProducerField.class.getField("getString");
      createProducerFieldBean(String.class, field, bean);
   }*/
   
   @Test(groups={"stub", "producerField", "enterpriseBeans", "stub"}, expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testProducerFieldIsNotBusinessField() throws Exception
   {
      assert false;
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4")
   public void testParameterizedReturnType() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("getFunnelWeaverSpider");
      createProducerFieldBean(FunnelWeaver.class, field, bean);
   }
   
   @Test(groups="producerField", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.4")
   public void testParameterizedReturnTypeWithWildcard() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("getAnotherFunnelWeaver");
      createProducerFieldBean(FunnelWeaver.class, field, bean);
   }
   
   @Test(groups={"stub", "producerField", "deployment"}) @SpecAssertion(section="3.4")
   public void testBeanDeclaresMultipleProducerFields()
   {
      assert false;
   }
   
   @Test(groups="producerField") @SpecAssertion(section={"3.4", "2.3.1"})
   public void testDefaultBindingType() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceTarantula");
      ProducerFieldBean<Tarantula> tarantulaModel = createProducerFieldBean(Tarantula.class, field, bean);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.1")
   public void testApiTypeForClassReturn() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceTarantula");
      ProducerFieldBean<Tarantula> tarantulaModel = createProducerFieldBean(Tarantula.class, field, bean);
      assert tarantulaModel.getTypes().size() == 6;
      assert tarantulaModel.getTypes().contains(Tarantula.class);
      assert tarantulaModel.getTypes().contains(DeadlySpider.class);
      assert tarantulaModel.getTypes().contains(Spider.class);
      assert tarantulaModel.getTypes().contains(Animal.class);
      assert tarantulaModel.getTypes().contains(DeadlyAnimal.class);
      assert tarantulaModel.getTypes().contains(Object.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.1")
   public void testApiTypeForInterfaceReturn() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("makeASpider");
      ProducerFieldBean<Animal> animalModel = createProducerFieldBean(Animal.class, field, bean);
      assert animalModel.getTypes().size() == 2;
      assert animalModel.getTypes().contains(Animal.class);
      assert animalModel.getTypes().contains(Object.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.1")
   public void testApiTypeForPrimitiveReturn() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("getWolfSpiderSize");
      ProducerFieldBean<Integer> intModel = createProducerFieldBean(int.class, field, bean);
      assert intModel.getTypes().size() == 2;
      assert intModel.getTypes().contains(int.class);
      assert intModel.getTypes().contains(Object.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section={"3.4.1", "2.2"})
   public void testApiTypeForArrayTypeReturn() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("getSpiders");
      ProducerFieldBean<Spider[]> spidersModel = createProducerFieldBean(Spider[].class, field, bean);
      assert spidersModel.getTypes().size() == 2;
      assert spidersModel.getTypes().contains(Spider[].class);
      assert spidersModel.getTypes().contains(Object.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.2")
   public void testBindingType() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceTameTarantula");
      ProducerFieldBean<Tarantula> tarantulaModel = createProducerFieldBean(Tarantula.class, field, bean);
      assert tarantulaModel.getBindingTypes().size() == 1;
      assert tarantulaModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.2")
   public void testScopeType() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceDaddyLongLegs");
      ProducerFieldBean<DaddyLongLegs> daddyLongLegsModel = createProducerFieldBean(DaddyLongLegs.class, field, bean);
      assert daddyLongLegsModel.getScopeType().equals(RequestScoped.class);
      
      // TODO Inherit scope from returned web bean?
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.2")
   public void testDeploymentType() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("getLadybirdSpider");
      ProducerFieldBean<LadybirdSpider> ladybirdSpiderModel = createProducerFieldBean(LadybirdSpider.class, field, bean);
      assert ladybirdSpiderModel.getDeploymentType().equals(Production.class);
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.2")
   public void testNamedField() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceBlackWidow");
      ProducerFieldBean<BlackWidow> blackWidowSpiderModel = createProducerFieldBean(BlackWidow.class, field, bean);
      assert blackWidowSpiderModel.getName().equals("blackWidow");
   }
   
   @Test(groups="producerField") @SpecAssertion(section="3.4.2")
   public void testDefaultNamedField() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceDaddyLongLegs");
      ProducerFieldBean<DaddyLongLegs> daddyLongLegsSpiderModel = createProducerFieldBean(DaddyLongLegs.class, field, bean);
      assert daddyLongLegsSpiderModel.getName().equals("produceDaddyLongLegs");
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.4")
   public void testDisposalFieldNonStatic()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.4")
   public void testDisposalFieldFieldDeclaredOnWebBeanImplementationClass()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.4")
   public void testDisposalFieldBindingAnnotations()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.4")
   public void testDisposalFieldDefaultBindingAnnotations()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.4")
   public void testDisposalFieldDoesNotResolveToProducerField()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.5")
   public void testDisposalFieldDeclaredOnEnabledBean()
   {
      // TODO Placeholder
      // TODO Move this
      
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.5")
   public void testBeanCanDeclareMultipleDisposalFields()
   {
      // TODO move this 
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalField"}) @SpecAssertion(section="3.3.5")
   public void testProducerFieldHasNoMoreThanOneDisposalField()
   {
      // TODO move this 
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="producerField") @SpecAssertion(section={"2.7.2", "3.4.2", "2.2"})
   public void testStereotype() throws Exception
   {
      SimpleBean<OtherSpiderProducer> bean = createSimpleBean(OtherSpiderProducer.class);
      manager.addBean(bean);
      Field field = OtherSpiderProducer.class.getField("produceWolfSpider");
      ProducerFieldBean<WolfSpider> wolfSpiderModel = createProducerFieldBean(WolfSpider.class, field, bean);
      assert wolfSpiderModel.getMergedStereotypes().getRequiredTypes().size() == 1;
      assert wolfSpiderModel.getMergedStereotypes().getRequiredTypes().contains(Animal.class);
      assert wolfSpiderModel.getScopeType().equals(RequestScoped.class);
   }
}
