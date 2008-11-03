package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Current;
import javax.webbeans.Dependent;

import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.AbstractEnterpriseBeanModel;
import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.beans.Bear;
import org.jboss.webbeans.test.beans.Cheetah;
import org.jboss.webbeans.test.beans.Cougar;
import org.jboss.webbeans.test.beans.Elephant;
import org.jboss.webbeans.test.beans.Giraffe;
import org.jboss.webbeans.test.beans.Leopard;
import org.jboss.webbeans.test.beans.Lion;
import org.jboss.webbeans.test.beans.Panther;
import org.jboss.webbeans.test.beans.Puma;
import org.jboss.webbeans.test.beans.Tiger;
import org.jboss.webbeans.test.util.Util;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SuppressWarnings( { "unchecked", "unused" })
public class EnterpriseBeanModelTest extends AbstractTest
{

   @Test
   public void testStateless()
   {
      EnterpriseBeanModel<Lion> lion = new EnterpriseBeanModel<Lion>(new SimpleAnnotatedType<Lion>(Lion.class), getEmptyAnnotatedType(Lion.class), manager);
      assert lion.getScopeType().equals(Dependent.class);
      Reflections.annotationSetMatches(lion.getBindingTypes(), Current.class);
      assert lion.getName().equals("lion");
   }

   @Test
   public void testStatelessDefinedInXml()
   {
      EnterpriseBeanModel<Giraffe> giraffe = Util.createEnterpriseBeanModel(Giraffe.class, manager);
      assert giraffe.getScopeType().equals(Dependent.class);
      assert Reflections.annotationSetMatches(giraffe.getBindingTypes(), Current.class);
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void testStatelessWithRequestScope()
   {
      EnterpriseBeanModel<Bear> bear = Util.createEnterpriseBeanModel(Bear.class, manager);
   }

   @Test(groups = "ejb3")
   public void testSingleton()
   {
      assert false;
   }

   @Test(groups = "ejb3")
   public void testSingletonWithRequestScope()
   {
      assert false;
   }

   @Test
   public void testStateful()
   {
      EnterpriseBeanModel<Tiger> tiger = Util.createEnterpriseBeanModel(Tiger.class, manager);
      assert Reflections.annotationSetMatches(tiger.getBindingTypes(), Synchronous.class);
      assert tiger.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove");
      assert tiger.getName() == null;
   }

   @Test
   public void testMultipleRemoveMethodsWithDestroys()
   {
      EnterpriseBeanModel<Elephant> elephant = Util.createEnterpriseBeanModel(Elephant.class, manager);
      assert elephant.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove2");
   }

   @Test(expectedExceptions=RuntimeException.class)
   public void testMultipleRemoveMethodsWithoutDestroys()
   {
      EnterpriseBeanModel<Puma> puma = Util.createEnterpriseBeanModel(Puma.class, manager);
   }

   @Test(expectedExceptions=RuntimeException.class)
   public void testMultipleRemoveMethodsWithMultipleDestroys()
   {
      EnterpriseBeanModel<Cougar> cougar = Util.createEnterpriseBeanModel(Cougar.class, manager);
   }

   @Test(expectedExceptions=RuntimeException.class)
   public void testNonStatefulEnterpriseBeanWithDestroys()
   {
      EnterpriseBeanModel<Cheetah> cheetah = Util.createEnterpriseBeanModel(Cheetah.class, manager);
   }

   @Test
   public void testRemoveMethodWithDefaultBinding()
   {
      EnterpriseBeanModel<Panther> panther = Util.createEnterpriseBeanModel(Panther.class, manager);
      assert panther.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove");
      assert panther.getRemoveMethod().getParameters().size() == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getType().equals(String.class);
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(panther.getRemoveMethod().getParameters().get(0).getBindingTypes(), Current.class);
   }

   @Test
   public void testMessageDriven()
   {
      EnterpriseBeanModel<Leopard> leopard = Util.createEnterpriseBeanModel(Leopard.class, manager);
      assert Reflections.annotationSetMatches(leopard.getBindingTypes(), Current.class);
   }

}
