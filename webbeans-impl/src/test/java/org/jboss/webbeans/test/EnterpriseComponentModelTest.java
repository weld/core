package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.annotationSetMatches;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.Current;
import javax.webbeans.Dependent;

import org.jboss.webbeans.ContainerImpl;
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
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Before;
import org.junit.Test;

public class EnterpriseComponentModelTest
{
   
private ContainerImpl container;
   
   private AnnotatedItem emptyAnnotatedItem;
   
   @Before
   public void before()
   {
      emptyAnnotatedItem = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStateless()
   {
      EnterpriseComponentModel<Lion> lion = new EnterpriseComponentModel<Lion>(new ClassAnnotatedItem(Lion.class), emptyAnnotatedItem, container);
      assert lion.getScopeType().annotationType().equals(Dependent.class);
      annotationSetMatches(lion.getBindingTypes(), Current.class);
      assert lion.getName().equals("lion");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testStatelessDefinedInXml()
   {
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      AnnotatedItem annotatedItem = new MutableAnnotatedItem(Giraffe.class, annotations);
      
      EnterpriseComponentModel<Giraffe> giraffe = new EnterpriseComponentModel<Giraffe>(new ClassAnnotatedItem(Giraffe.class), annotatedItem, container);
      assert giraffe.getScopeType().annotationType().equals(Dependent.class);
      annotationSetMatches(giraffe.getBindingTypes(), Current.class);
   }
   
   @Test
   public void testStatelessWithRequestScope()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Bear>(new ClassAnnotatedItem(Bear.class), emptyAnnotatedItem, container);
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

      EnterpriseComponentModel<Tiger> tiger = new EnterpriseComponentModel<Tiger>(new ClassAnnotatedItem(Tiger.class), emptyAnnotatedItem, container);
      annotationSetMatches(tiger.getBindingTypes(), Synchronous.class);
      assert tiger.getRemoveMethod().getMethod().getName().equals("remove");
      assert tiger.getName() == null;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithDestroys()
   {

      EnterpriseComponentModel<Elephant> elephant = new EnterpriseComponentModel<Elephant>(new ClassAnnotatedItem(Elephant.class), emptyAnnotatedItem, container);
      assert elephant.getRemoveMethod().getMethod().getName().equals("remove2");
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMultipleRemoveMethodsWithoutDestroys()
   {
      boolean exception = false;
      try
      {
         new EnterpriseComponentModel<Puma>(new ClassAnnotatedItem(Puma.class), emptyAnnotatedItem, container);
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
         new EnterpriseComponentModel<Cougar>(new ClassAnnotatedItem(Cougar.class), emptyAnnotatedItem, container);
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
         new EnterpriseComponentModel<Cheetah>(new ClassAnnotatedItem(Cheetah.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
   }
   
   @Test
   public void testRemoveMethodWithDefaultBinding()
   {

      EnterpriseComponentModel<Panther> panther = new EnterpriseComponentModel<Panther>(new ClassAnnotatedItem(Panther.class), emptyAnnotatedItem, container);
      
      assert panther.getRemoveMethod().getMethod().getName().equals("remove");
      assert panther.getRemoveMethod().getParameters().size() == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getType().equals(String.class);
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes().length == 1;
      assert panther.getRemoveMethod().getParameters().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testMessageDriven()
   {
      EnterpriseComponentModel<Leopard> leopard = new EnterpriseComponentModel<Leopard>(new ClassAnnotatedItem(Leopard.class), emptyAnnotatedItem, container);
      annotationSetMatches(leopard.getBindingTypes(), Current.class);
   }

}
