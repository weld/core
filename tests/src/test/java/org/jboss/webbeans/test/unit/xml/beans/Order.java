package org.jboss.webbeans.test.unit.xml.beans;

import javax.context.RequestScoped;
import javax.inject.Initializer;

import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestDeploymentType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestInterceptorBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestStereotype;

@RequestScoped
@TestBindingType
@TestInterceptorBindingType
@TestStereotype
@TestDeploymentType
public class Order
{   
   private int val;
   
   private String[] strArr;
   
   @Initializer
   public Order()
   {
      this.val = 0;
   }
   
   public Order(int val)
   {
      this.val = val;
   }
   
   public int getVal()
   {
      return this.val;
   }
   
   public String[] getStrArr()
   {
      return this.strArr;
   }
}
