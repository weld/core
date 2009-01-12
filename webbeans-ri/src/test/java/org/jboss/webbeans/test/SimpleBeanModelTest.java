package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.webbeans.AnnotationLiteral;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentConstructorException;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.binding.CurrentBinding;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.test.annotations.HeavyDuty;
import org.jboss.webbeans.test.annotations.Motorized;
import org.jboss.webbeans.test.beans.Cow;
import org.jboss.webbeans.test.beans.Donkey;
import org.jboss.webbeans.test.beans.Duck;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Sheep;
import org.jboss.webbeans.test.beans.Tractor;
import org.jboss.webbeans.test.beans.Turkey;
import org.jboss.webbeans.test.beans.broken.Goose;
import org.jboss.webbeans.test.beans.broken.ParameterizedBean;
import org.jboss.webbeans.test.beans.broken.OuterBean.InnerBean;
import org.jboss.webbeans.test.beans.broken.OuterBean.StaticInnerBean;
import org.jboss.webbeans.test.beans.nonBeans.EnterpriseBeanWebBean;
import org.jboss.webbeans.test.beans.nonBeans.FilterBean;
import org.jboss.webbeans.test.beans.nonBeans.HttpSessionListenerBean;
import org.jboss.webbeans.test.beans.nonBeans.ServletBean;
import org.jboss.webbeans.test.beans.nonBeans.ServletContextListenerBean;
import org.jboss.webbeans.test.beans.nonBeans.ServletRequestListenerBean;
import org.jboss.webbeans.test.beans.nonBeans.UIComponentBean;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class SimpleBeanModelTest extends AbstractTest
{   
   
   //*** BEAN CLASS CHECKS ****//
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2")
   public void testAbstractClassDeclaredInJavaIsNotAllowed()
   {
      SimpleBean.of(Cow.class, manager);
   }
   
   @Test(groups="innerClass") @SpecAssertion(section="3.2")
   public void testStaticInnerClassDeclaredInJavaAllowed()
   {
      SimpleBean.of(StaticInnerBean.class, manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups="innerClass") @SpecAssertion(section="3.2")
   public void testNonStaticInnerClassDeclaredInJavaNotAllowed()
   {
      SimpleBean.of(InnerBean.class, manager);
   }
   
   @SuppressWarnings("unchecked")
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2")
   public void testParameterizedClassDeclaredInJavaIsNotAllowed()
   {
      SimpleBean.of(ParameterizedBean.class, manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "interceptors", "decorators"}) @SpecAssertion(section="3.2")
   public void testClassCannotBeInterceptorAndDecorator()
   {
      
   }
   
   @Test(groups="stub")
   public void testEntitiesNotDiscoveredAsSimpleBeans()
   {
      assert false;
   }
   
   @Test
   public void testClassesImplementingServletInterfacesNotDiscoveredAsSimpleBeans()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(new HashSet<Class<?>>(Arrays.asList(FilterBean.class, HttpSessionListenerBean.class, ServletBean.class, ServletContextListenerBean.class, ServletRequestListenerBean.class)), null, new HashSet<Class<?>>()));
      webBeansBootstrap.boot();
      assert manager.getBeans().size() == BUILT_IN_BEANS;
   }
   
   @Test
   public void testClassesImplementingEnterpriseBeanInterfaceNotDiscoveredAsSimpleBean()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(new HashSet<Class<?>>(Arrays.asList(EnterpriseBeanWebBean.class)), null, new HashSet<Class<?>>()));
      webBeansBootstrap.boot();
      assert manager.getBeans().size() == BUILT_IN_BEANS;
   }
   
   @Test
   public void testClassExtendingUiComponentNotDiscoveredAsSimpleBean()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(new HashSet<Class<?>>(Arrays.asList(UIComponentBean.class)), null, new HashSet<Class<?>>()));
      webBeansBootstrap.boot();
      assert manager.getBeans().size() == BUILT_IN_BEANS;
   }
   
   @Test(groups="stub")
   public void testEjbsNotDiscoveredAsSimpleBean()
   {
      
   }
   
   @Test(groups={"stub", "producerMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresProducerMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups={"stub", "disposalMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresDisposalMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups={"stub", "observerMethod", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testBeanDeclaredInXmlIgnoresObserverMethodDeclaredInJava()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testAbstractClassDeclaredInXmlIsNotAllowed()
   {
      
   }
   
   @Test(groups={"stub", "innerClass", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testStaticInnerClassDeclaredInXmlAllowed()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "innerClass", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testNonStaticInnerClassDeclaredInXmlNotAllowed()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "webbeansxml"}) @SpecAssertion(section="3.2.4")
   public void testParameterizedClassDeclaredInXmlIsNotAllowed()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "interceptors"}) @SpecAssertion(section="3.2.4")
   public void testClassHasInterceptorInJavaMustHaveInterceptorInXml()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "interceptors"}) @SpecAssertion(section="3.2.4")
   public void testClassHasDecoratorInJavaMustHaveDecoratorInXml()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testInitializerAnnotatedConstructor()
   {
      AnnotatedConstructor<Sheep> constructor = SimpleBean.of(Sheep.class, manager).getConstructor();
      assert constructor.getDeclaringClass().getType().equals(Sheep.class);
      assert constructor.getParameters().size() == 2;
      
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (AnnotatedParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Double.class);
      assert map.get(String.class).size() == 1;
      assert map.get(String.class).contains(new CurrentBinding());
      assert map.get(Double.class).size() == 1;
      assert map.get(Double.class).contains(new CurrentBinding());
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testImplicitConstructorUsed()
   {
      AnnotatedConstructor<Order> constructor = SimpleBean.of(Order.class, manager).getConstructor();
      assert constructor.getDeclaringClass().getType().equals(Order.class);
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testEmptyConstructorUsed()
   {
      AnnotatedConstructor<Donkey> constructor = SimpleBean.of(Donkey.class, manager).getConstructor();
      assert constructor.getDeclaringClass().getType().equals(Donkey.class);
      assert constructor.getParameters().size() == 0;
   }
   
   @Test @SpecAssertion(section="3.2.5.1")
   public void testInitializerAnnotatedConstructorUsedOverEmptyConstuctor()
   {
      AnnotatedConstructor<Turkey> constructor = SimpleBean.of(Turkey.class, manager).getConstructor();
      assert constructor.getParameters().size() == 2;
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (AnnotatedParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Integer.class);
   }
   
   @Test(expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.5.1")
   public void testTooManyInitializerAnnotatedConstructor()
   {
      SimpleBean.of(Goose.class, manager);
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "disposalMethod"}) @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasDisposesParameter()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"stub", "observerMethod"}) @SpecAssertion(section="3.2.5.1")
   public void testConstructorHasObservesParameter()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="3.2.5.2")
   public void testImplicitConstructorDeclaredInXmlUsed()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"})  @SpecAssertion(section="3.2.5.2")
   public void testEmptyConstructorDeclaredInXmlUsed()
   {
      AnnotatedConstructor<Donkey> constructor = SimpleBean.of(Donkey.class, manager).getConstructor();
      assert constructor.getParameters().size() == 0;
      assert false;
   }
   
   @Test(expectedExceptions=NonexistentConstructorException.class, groups={"stub", "webbeansxml"}) @SpecAssertion(section="3.2.5.2")
   public void testConstructorDeclaredInXmlDoesNotExist()
   {
      assert false;
   }
   
   @Test(groups={"stub", "webbeansxml"}) @SpecAssertion(section="3.2.5.2")
   public void testConstructorDeclaredInXmlIgnoresBindingTypesDeclaredInJava()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="3.2.5.3")
   public void testBindingTypeAnnotatedConstructor()
   {
      AnnotatedConstructor<Duck> constructor = SimpleBean.of(Duck.class, manager).getConstructor();
      assert constructor.getDeclaringClass().getType().equals(Duck.class);
      assert constructor.getParameters().size() == 2;
      Map<Class<?>, Set<? extends Annotation>> map = new HashMap<Class<?>, Set<? extends Annotation>>();
      for (AnnotatedParameter<Object> parameter : constructor.getParameters())
      {
         map.put(parameter.getType(), parameter.getBindingTypes());
      }
      assert map.containsKey(String.class);
      assert map.containsKey(Integer.class);
      assert map.get(String.class).size() == 1;
      assert map.get(String.class).contains(new CurrentBinding());
      assert map.get(Integer.class).size() == 1;
      assert map.get(Integer.class).contains(new SynchronousAnnotationLiteral());
   }
   
   @Test(groups="specializationInherit") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsBindingTypes()
   {
      SimpleBean<Tractor> bean = SimpleBean.of(Tractor.class, manager);
      assert bean.getBindings().size()==2;
      assert bean.getBindings().contains( new AnnotationLiteral<Motorized>() {} );
      assert bean.getBindings().contains( new AnnotationLiteral<HeavyDuty>() {} );
   }
   
   @Test(groups="specializationInherit") @SpecAssertion(section="3.2.6")
   public void testSpecializedClassInheritsName()
   {
      SimpleBean<Tractor> bean = SimpleBean.of(Tractor.class, manager);
      assert bean.getName()!=null;
      assert bean.getName().equals("plough");
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.2.6")
   public void testLessSpecializedClassNotInstantiated()
   {
      assert false;
   }
   
   @Test(groups={"stub", "specialization"},expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.2.6")
   public void testSpecializedClassMustExtendAnotherWebBean()
   {
      assert false;
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.2.6")
   public void testSpecializedClassDeclaredInXmlInheritsBindingTypes()
   {
      assert false;
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.2.6")
   public void testSpecializedClassDeclaredInXmlInheritsName()
   {
      assert false;
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.2.6")
   public void testLessSpecializedClassDeclaredInXmlNotInstantiated()
   {
      assert false;
   }
   
}
