package org.jboss.weld.tests.unit.cluster;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class Foo implements Serializable
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
