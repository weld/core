package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Named;
import javax.webbeans.Production;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.bindings.NamedBinding;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.SynchronousBinding;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComponentInstanceTest
{
   
   private ContainerImpl container;
   
   private static AnnotatedItem emptyAnnotatedItem;
   
   private static AnnotatedItem currentSynchronousOrder;
   
   @BeforeClass
   public static void beforeClass()
   {
      Map<Class<? extends Annotation>, Annotation> orderXmlAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();
      orderXmlAnnotations.put(Current.class, new CurrentBinding());
      orderXmlAnnotations.put(Synchronous.class, new SynchronousBinding());
      orderXmlAnnotations.put(Named.class, new NamedBinding()
      {
         public String value()
         {
            return "currentSynchronousOrder";
         }
      });
      currentSynchronousOrder = new MutableAnnotatedItem(Order.class, orderXmlAnnotations);
      
      emptyAnnotatedItem = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
   }
   
   @Before
   public void before()
   {
      // TODO Probably need a mock container
      container = new ContainerImpl();
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new ClassAnnotatedItem(AnimalStereotype.class));
      container.getStereotypeManager().addStereotype(animalStereotype);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testOrder()
   {
      
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container);
      assert Production.class.equals(order.getComponentType().annotationType());
      assert "order".equals(order.getName());
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert order.getScopeType().annotationType().equals(Dependent.class);
      //assert order.getTypes() == ??
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testCurrentSynchronousOrder()
   {
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new ClassAnnotatedItem(Order.class), currentSynchronousOrder, container);
      assert Production.class.equals(order.getComponentType().annotationType());
      assert "currentSynchronousOrder".equals(order.getName());
      assert order.getBindingTypes().size() == 2;
      for (Annotation annotation : order.getBindingTypes())
      {
         // TODO Write a utility class to do this type of test
         assert annotation.annotationType().equals(Current.class) || annotation.annotationType().equals(Synchronous.class);
      }
      assert order.getScopeType().annotationType().equals(Dependent.class);
   }
   
   @Test
   public void testGorilla()
   {
      ComponentInstance<Gorilla> gorilla = new ComponentInstanceImpl<Gorilla>(new ClassAnnotatedItem(Gorilla.class), emptyAnnotatedItem, container);
      assert gorilla.getName() == null;
      // TODO Ensure that the a java declared component declares a deployment type
      //assert gorilla.getComponentType() == null;
      assert gorilla.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert gorilla.getScopeType().annotationType().equals(Dependent.class);
   }
}
