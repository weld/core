package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Current;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.AbstractEnterpriseComponentModel;
import org.jboss.webbeans.model.EnterpriseComponentModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.components.Bear;
import org.jboss.webbeans.test.components.Cheetah;
import org.jboss.webbeans.test.components.Cougar;
import org.jboss.webbeans.test.components.Elephant;
import org.jboss.webbeans.test.components.Giraffe;
import org.jboss.webbeans.test.components.Leopard;
import org.jboss.webbeans.test.components.Lion;
import org.jboss.webbeans.test.components.Panther;
import org.jboss.webbeans.test.components.Puma;
import org.jboss.webbeans.test.components.Tiger;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EnterpriseComponentModelTest extends AbstractTest
{
   
   private ManagerImpl container;
   
   @SuppressWarnings("unchecked")
   private AnnotatedType emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType<Object>(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockManagerImpl(null);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testSingleStereotype()
   {
	   assert false;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStateless()
   {
      EnterpriseComponentModel<Lion> lion = new EnterpriseComponentModel<Lion>(new SimpleAnnotatedType<Lion>(Lion.class), emptyAnnotatedItem, container);
      assert lion.getScopeType().equals(Dependent.class);
      Reflections.annotationSetMatches(lion.getBindingTypes(), Current.class);
      assert lion.getName().equals("lion");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStatelessDefinedInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Giraffe.class, annotations);
      
      EnterpriseComponentModel<Giraffe> giraffe = new EnterpriseComponentModel<Giraffe>(new SimpleAnnotatedType(Giraffe.class), annotatedItem, container);
      assert giraffe.getScopeType().equals(Dependent.class);
      Reflections.annotationSetMatches(giraffe.getBindingTypes(), Current.class);
   }
   
   @Test
   public void testStatelessWithRequestScope()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Bear>(new SimpleAnnotatedType<Bear>(Bear.class), getEmptyAnnotatedItem(Bear.class), container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   // TODO Need EJB3.1 @Test
   public void testSingleton()
   {
      //ComponentMetaModel<Lion> lion = new ComponentMetaModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      //assert lion.getComponentType().equals(ComponentType.ENTERPRISE);
      //assert lion.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   // TODO Need EJB3.1 @Test
   public void testSingletonWithRequestScope()
   {
      //ComponentMetaModel<Lion> lion = new ComponentMetaModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      //assert lion.getComponentType().equals(ComponentType.ENTERPRISE);
      //assert lion.getScopeType().annotationType().equals(ApplicationScoped.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStateful()
   {

      AbstractEnterpriseComponentModel<Tiger> tiger = new EnterpriseComponentModel<Tiger>(new SimpleAnnotatedType(Tiger.class), emptyAnnotatedItem, container);
      Reflections.annotationSetMatches(tiger.getBindingTypes(), Synchronous.class);
      assert tiger.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove");
      assert tiger.getName() == null;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithDestroys()
   {

      AbstractEnterpriseComponentModel<Elephant> elephant = new EnterpriseComponentModel<Elephant>(new SimpleAnnotatedType(Elephant.class), emptyAnnotatedItem, container);
      assert elephant.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove2");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithoutDestroys()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Puma>(new SimpleAnnotatedType(Puma.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithMultipleDestroys()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Cougar>(new SimpleAnnotatedType(Cougar.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testNonStatefulEnterpriseComponentWithDestroys()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Cheetah>(new SimpleAnnotatedType(Cheetah.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testRemoveMethodWithDefaultBinding()
   {

      AbstractEnterpriseComponentModel<Panther> panther = new EnterpriseComponentModel<Panther>(new SimpleAnnotatedType<Panther>(Panther.class), emptyAnnotatedItem, container);
      
      assert panther.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("remove");
      assert panther.getRemoveMethod().getParameters().size() == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getType().equals(String.class);
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(panther.getRemoveMethod().getParameters().get(0).getBindingTypes(), Current.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMessageDriven()
   {
      AbstractEnterpriseComponentModel<Leopard> leopard = new EnterpriseComponentModel<Leopard>(new SimpleAnnotatedType(Leopard.class), emptyAnnotatedItem, container);
      Reflections.annotationSetMatches(leopard.getBindingTypes(), Current.class);
   }

}
