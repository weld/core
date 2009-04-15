package org.jboss.webbeans.test.unit.xml.beans;

import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestInterceptorBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestStereotype;

@TestBindingType
@TestInterceptorBindingType
@TestStereotype
public class Order
{
   public int val;
   
   public String[] strArray;
   
   public Order(int val)
   {
      this.val = val;
   }
   
   public int getVal()
   {
      return val;
   }
}
