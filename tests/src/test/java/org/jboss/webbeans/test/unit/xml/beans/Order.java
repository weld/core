package org.jboss.webbeans.test.unit.xml.beans;

import javax.context.Dependent;
import javax.inject.Current;
import javax.inject.Initializer;
import javax.inject.manager.Manager;

import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestDeploymentType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestInterceptorBindingType;
import org.jboss.webbeans.test.unit.xml.beans.annotationtype.TestStereotype;

@TestBindingType
@TestInterceptorBindingType
@TestStereotype
@TestDeploymentType
public class Order
{
   private boolean active;
   
   @Initializer
   public Order(@Current Manager manager)
   {
      if (manager.getContext(Dependent.class).isActive())
      {
         active = true;
      }
   }
   
   public boolean isActive()
   {
      return active;
   }
}
