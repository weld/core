package org.jboss.webbeans.test;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Current;
import javax.webbeans.Dependent;
import javax.webbeans.Production;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.StereotypeMetaModel;
import org.jboss.webbeans.test.components.AnimalStereotype;
import org.jboss.webbeans.test.components.Gorilla;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.MutableAnnotatedWebBean;
import org.junit.Before;
import org.junit.Test;

public class ComponentInstanceTest
{
   
   private ContainerImpl container;
   
   @Before
   public void before()
   {
      // TODO Probably need a mock container
      container = new ContainerImpl();
      StereotypeMetaModel animalStereotype = new StereotypeMetaModel(new MutableAnnotatedWebBean(AnimalStereotype.class));
      container.getStereotypeManager().addStereotype(animalStereotype);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testOrder()
   {
      
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new MutableAnnotatedWebBean(Order.class), container);
      assert Production.class.equals(order.getComponentType().annotationType());
      assert "order".equals(order.getName());
      assert order.getBindingTypes().size() == 1;
      order.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert order.getScopeType().annotationType().equals(Dependent.class);
      //assert order.getTypes() == ??
   }
   
   @Test
   public void testGorilla()
   {
      ComponentInstance<Gorilla> gorilla = new ComponentInstanceImpl<Gorilla>(new MutableAnnotatedWebBean(Gorilla.class), container);
      assert gorilla.getName() == null;
      // TODO Ensure that the a java declared component declares a deployment type
      //assert gorilla.getComponentType() == null;
      assert gorilla.getBindingTypes().iterator().next().annotationType().equals(Current.class);
      assert gorilla.getScopeType().annotationType().equals(Dependent.class);
   }
}
