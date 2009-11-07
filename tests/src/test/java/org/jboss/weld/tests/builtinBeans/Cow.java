package org.jboss.weld.tests.builtinBeans;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Cow
{
   
   private String name;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
