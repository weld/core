package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;
import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentConstructorException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.injectable.InjectableParameter;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.HeavyDuty;
import org.jboss.webbeans.test.annotations.Motorized;
import org.jboss.webbeans.test.beans.Animal;
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
import org.jboss.webbeans.test.beans.broken.Goose;
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
      createSimpleModel(Cow.class, manager);
   }
   
   @Test(groups="innerClass") @SpecAssertion(section="3.2")
   public void testStaticInnerClassDeclaredInJavaAllowed()
   {
      createSimpleModel(StaticInnerBean.class, manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="innerClass") @SpecAssertion(section="3.2")
   public void testNonStaticInnerClassDeclaredInJavaNotAllowed()
   {
      createSimpleModel(InnerBean.class, manager);
   }
   
   @SuppressWarnings("unchecked")
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2")
   public void testParameterizedClassDeclaredInJavaIsNotAllowed()
   {
      createSimpleModel(ParameterizedBean.class, manager);
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
   
   @Test(groups={"producerMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresProducerMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups={"disposalMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresDisposalMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups={"observerMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresObserverMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="webbeansxml") @SpecAssertion(section="3.2.4")
   public void testAbstractClassDeclaredInXmlIsNotAllowed()
   {
      
   }
   
   @Test(groups={"innerClass", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testStaticInnerClassDeclaredInXmlAllowed()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"innerClass", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testNonStaticInnerClassDeclaredInXmlNotAllowed()
   {
      assert false;
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
      SimpleConstructor<Sheep> constructor = createSimpleModel(Sheep.class, manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Sheep.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 2;
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[0].equals(String.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[1].equals(Double.class);
      assert constructor.getParameters().size() == 2;
      
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (InjectableParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Double.class);
      assert map.get(String.class).size() == 1;
      assert map.get(String.class).contains(new CurrentAnnotationLiteral());
      assert map.get(Double.class).size() == 1;
      assert map.get(Double.class).contains(new CurrentAnnotationLiteral());
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testImplicitConstructorUsed()
   {
      SimpleConstructor<Order> constructor = createSimpleModel(Order.class, manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Order.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testEmptyConstructorUsed()
   {
      SimpleConstructor<Donkey> constructor = createSimpleModel(Donkey.class, manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Donkey.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testInitializerAnnotatedConstructorUsedOverEmptyConstuctor()
   {
      SimpleConstructor<Turkey> constructor = createSimpleModel(Turkey.class, manager).getConstructor();
      assert constructor.getParameters().size() == 2;
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (InjectableParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Integer.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.5.1")
   public void testTooManyInitializerAnnotatedConstructor()
   {
      createSimpleWebBean(Goose.class, manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="disposalMethod") @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasDisposesParameter()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="observerMethod") @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasObservesParameter()
   {
      assert false;
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testImplicitConstructorDeclaredInXmlUsed()
   {
      assert false;
   }
   
   @Test(groups="webbeansxml") @SpecAssertion(section="3.2.5.2")
   public void testEmptyConstructorDeclaredInXmlUsed()
   {
      SimpleConstructor<Donkey> constructor = createSimpleModel(Donkey.class, manager).getConstructor();
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
      SimpleConstructor<Duck> constructor = createSimpleModel(Duck.class, manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Duck.class);
      assert constructor.getParameters().size() == 2;
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (InjectableParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Integer.class);
      assert map.get(String.class).size() == 1;
      assert map.get(String.class).contains(new CurrentAnnotationLiteral());
      assert map.get(Integer.class).size() == 1;
      assert map.get(Integer.class).contains(new SynchronousAnnotationLiteral());
   }
   
   @Test(groups="specializationInherit") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsBindingTypes()
   {
      SimpleBeanModel<Tractor> bean = createSimpleModel(Tractor.class, manager);
      assert bean.getBindingTypes().size()==2;
      assert bean.getBindingTypes().contains( new AnnotationLiteral<Motorized>() {} );
      assert bean.getBindingTypes().contains( new AnnotationLiteral<HeavyDuty>() {} );
   }
   
   @Test(groups="specializationInherit") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsName()
   {
      SimpleBeanModel<Tractor> bean = createSimpleModel(Tractor.class, manager);
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
