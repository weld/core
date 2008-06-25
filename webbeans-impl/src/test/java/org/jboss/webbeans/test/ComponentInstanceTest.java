package org.jboss.webbeans.test;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Production;

import org.jboss.webbeans.ComponentInstanceImpl;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.MutableEnhancedAnnotatedElement;
import org.junit.Test;

public class ComponentInstanceTest
{

   @Test
   public void testMetaModel()
   {
      ComponentInstance<Order> order = new ComponentInstanceImpl<Order>(new MutableEnhancedAnnotatedElement(Order.class));
      assert Production.class.equals(order.getComponentType().annotationType());
      assert "order".equals(order.getName());
   }
   
}
