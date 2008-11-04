package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;
import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.util.Iterator;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentConstructorException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.HeavyDuty;
import org.jboss.webbeans.test.annotations.Motorized;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.Chicken;
import org.jboss.webbeans.test.beans.Cow;
import org.jboss.webbeans.test.beans.DeadlyAnimal;
import org.jboss.webbeans.test.beans.DeadlySpider;
import org.jboss.webbeans.test.beans.Donkey;
import org.jboss.webbeans.test.beans.Duck;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Sheep;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.Tractor;
import org.jboss.webbeans.test.beans.Turkey;
import org.jboss.webbeans.test.beans.broken.ParameterizedBean;
import org.jboss.webbeans.test.beans.broken.OuterBean.InnerBean;
import org.jboss.webbeans.test.beans.broken.OuterBean.StaticInnerBean;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class SimpleBeanModelTest extends AbstractTest
{   
   
   //*** BEAN CLASS CHECKS ****//
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2")
   public void testAbstractClassDeclaredInJavaIsNotAllowed()
   {
      new SimpleBeanModel<Cow>(new SimpleAnnotatedType<Cow>(Cow.class), getEmptyAnnotatedType(Cow.class), manager);
   }
   
   @Test(groups="innerClass") @SpecAssertion(section="3.2")
   public void testStaticInnerClassDeclaredInJavaAllowed()
   {
      new SimpleBeanModel<StaticInnerBean>(new SimpleAnnotatedType<StaticInnerBean>(StaticInnerBean.class), getEmptyAnnotatedType(StaticInnerBean.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="innerClass") @SpecAssertion(section="3.2")
   public void testNonStaticInnerClassDeclaredInJavaNotAllowed()
   {
      new SimpleBeanModel<InnerBean>(new SimpleAnnotatedType<InnerBean>(InnerBean.class), getEmptyAnnotatedType(InnerBean.class), manager);
   }
   
   @SuppressWarnings("unchecked")
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2")
   public void testParameterizedClassDeclaredInJavaIsNotAllowed()
   {
      new SimpleBeanModel<ParameterizedBean>(new SimpleAnnotatedType<ParameterizedBean>(ParameterizedBean.class), getEmptyAnnotatedType(ParameterizedBean.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"interceptors", "decorators"}) @SpecAssertion(section="3.2")
   public void testClassCannotBeInterceptorAndDecorator()
   {
      
   }
   
   @Test(groups="deployment") @SpecAssertion(section="3.2")
   public void testOnlyOneWebBeanPerAnnotatedClass()
   {
      
   }
   
   @Test @SpecAssertion(section="3.2.2")
   public void testApiTypes()
   {
      Bean<Tarantula> bean = createSimpleWebBean(Tarantula.class, manager);
      assert bean.getTypes().size() == 6;
      assert bean.getTypes().contains(Tarantula.class);
      assert bean.getTypes().contains(Spider.class);
      assert bean.getTypes().contains(Animal.class);
      assert bean.getTypes().contains(Object.class);
      assert bean.getTypes().contains(DeadlySpider.class);
      assert bean.getTypes().contains(DeadlyAnimal.class);
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresProducerMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups="disposalMethod") @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresDisposalMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups="observerMethod") @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresObserverMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.4")
   public void testAbstractClassDeclaredInXmlIsNotAllowed()
   {
      new SimpleBeanModel<Cow>(new SimpleAnnotatedType<Cow>(Cow.class), getEmptyAnnotatedType(Cow.class), manager);
   }
   
   @Test(groups="innerClass") @SpecAssertion(section="3.2.4")
   public void testStaticInnerClassDeclaredInXmlAllowed()
   {
      new SimpleBeanModel<StaticInnerBean>(new SimpleAnnotatedType<StaticInnerBean>(StaticInnerBean.class), getEmptyAnnotatedType(StaticInnerBean.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="innerClass") @SpecAssertion(section="3.2.4")
   public void testNonStaticInnerClassDeclaredInXmlNotAllowed()
   {
      new SimpleBeanModel<InnerBean>(new SimpleAnnotatedType<InnerBean>(InnerBean.class), getEmptyAnnotatedType(InnerBean.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="webbeansxml") @SpecAssertion(section="3.2.4")
   public void testParameterizedClassDeclaredInXmlIsNotAllowed()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"interceptors"}) @SpecAssertion(section="3.2.4")
   public void testClassHasInterceptorInJavaMustHaveInterceptorInXml()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"interceptors"}) @SpecAssertion(section="3.2.4")
   public void testClassHasDecoratorInJavaMustHaveDecoratorInXml()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testInitializerAnnotatedConstructor()
   {
      SimpleConstructor<Sheep> constructor = new SimpleBeanModel<Sheep>(new SimpleAnnotatedType<Sheep>(Sheep.class), getEmptyAnnotatedType(Sheep.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Sheep.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 2;
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[0].equals(String.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[1].equals(Double.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Double.class);
      assert constructor.getParameters().get(0).getBindingTypes().size() == 1;
      assert constructor.getParameters().get(0).getBindingTypes().contains(new CurrentAnnotationLiteral());
      assert constructor.getParameters().get(1).getBindingTypes().size() == 1;
      assert constructor.getParameters().get(1).getBindingTypes().contains(new CurrentAnnotationLiteral());
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testImplicitConstructorUsed()
   {
      SimpleConstructor<Order> constructor = new SimpleBeanModel<Order>(new SimpleAnnotatedType<Order>(Order.class), getEmptyAnnotatedType(Order.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Order.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testEmptyConstructorUsed()
   {
      SimpleConstructor<Donkey> constructor = new SimpleBeanModel<Donkey>(new SimpleAnnotatedType<Donkey>(Donkey.class), getEmptyAnnotatedType(Donkey.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Donkey.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testInitializerAnnotatedConstructorUsedOverEmptyConstuctor()
   {
      SimpleConstructor<Turkey> constructor = new SimpleBeanModel<Turkey>(new SimpleAnnotatedType<Turkey>(Turkey.class), getEmptyAnnotatedType(Turkey.class), manager).getConstructor();
      assert constructor.getParameters().size() == 2;
      Iterator<InjectableParameter<?>> it = constructor.getParameters().iterator();
      assert it.next().getType().equals(String.class);
      assert it.next().getType().equals(Integer.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.5.1")
   public void testTooManyInitializerAnnotatedConstructor()
   {
      new SimpleBeanModel<Chicken>(new SimpleAnnotatedType<Chicken>(Chicken.class), getEmptyAnnotatedType(Chicken.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="disposalMethod") @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasDisposesParameter()
   {
      new SimpleBeanModel<Chicken>(new SimpleAnnotatedType<Chicken>(Chicken.class), getEmptyAnnotatedType(Chicken.class), manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="observerMethod") @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasObservesParameter()
   {
      new SimpleBeanModel<Chicken>(new SimpleAnnotatedType<Chicken>(Chicken.class), getEmptyAnnotatedType(Chicken.class), manager);
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testImplicitConstructorDeclaredInXmlUsed()
   {
      SimpleConstructor<Order> constructor = new SimpleBeanModel<Order>(new SimpleAnnotatedType<Order>(Order.class), getEmptyAnnotatedType(Order.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Order.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
      assert false;
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testEmptyConstructorDeclaredInXmlUsed()
   {
      SimpleConstructor<Donkey> constructor = new SimpleBeanModel<Donkey>(new SimpleAnnotatedType<Donkey>(Donkey.class), getEmptyAnnotatedType(Donkey.class), manager).getConstructor();      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Order.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
      assert false;
   }
   
   @Test(expectedExceptions=NonexistentConstructorException.class, groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testConstructorDeclaredInXmlDoesNotExist()
   {
      assert false;
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testConstructorDeclaredInXmlIgnoresBindingTypesDeclaredInJava()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="3.2.5.3")
   public void testBindingTypeAnnotatedConstructor()
   {
      SimpleConstructor<Duck> constructor = new SimpleBeanModel<Duck>(new SimpleAnnotatedType<Duck>(Duck.class), getEmptyAnnotatedType(Duck.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Duck.class);
      assert constructor.getParameters().size() == 2;
      Iterator<InjectableParameter<?>> it = constructor.getParameters().iterator();
      assert it.next().getType().equals(String.class);
      assert it.next().getType().equals(Integer.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Integer.class);
      assert constructor.getParameters().get(0).getBindingTypes().size() == 1;
      assert constructor.getParameters().get(0).getBindingTypes().contains(new CurrentAnnotationLiteral());
      assert constructor.getParameters().get(1).getBindingTypes().size() == 1;
      assert constructor.getParameters().get(1).getBindingTypes().contains(new SynchronousAnnotationLiteral());
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsBindingTypes()
   {
      SimpleBeanModel<Tractor> bean = new SimpleBeanModel<Tractor>(new SimpleAnnotatedType<Tractor>(Tractor.class), getEmptyAnnotatedType(Tractor.class), manager);
      assert bean.getBindingTypes().size()==2;
      assert bean.getBindingTypes().contains( new AnnotationLiteral<Motorized>() {} );
      assert bean.getBindingTypes().contains( new AnnotationLiteral<HeavyDuty>() {} );
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsName()
   {
      SimpleBeanModel<Tractor> bean = new SimpleBeanModel<Tractor>(new SimpleAnnotatedType<Tractor>(Tractor.class), getEmptyAnnotatedType(Tractor.class), manager);
      assert bean.getName()!=null;
      assert bean.getName().equals("plough");
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testLessSpecializedClassNotInstantiated()
   {
      assert false;
   }
   
   @Test(groups="specialization",expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.6")
   public void testSpecializedClassMustExtendAnotherWebBean()
   {
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassDeclaredInXmlInheritsBindingTypes()
   {
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassDeclaredInXmlInheritsName()
   {
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.2.6")
   public void testLessSpecializedClassDeclaredInXmlNotInstantiated()
   {
      assert false;
   }
   
}
