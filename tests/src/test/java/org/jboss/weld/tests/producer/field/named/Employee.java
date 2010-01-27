package org.jboss.weld.tests.producer.field.named;

import java.io.Serializable;


public class Employee implements Serializable
{

   private String name;
   
   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

}
