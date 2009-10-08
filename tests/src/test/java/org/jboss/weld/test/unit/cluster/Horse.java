package org.jboss.weld.test.unit.cluster;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Horse implements Serializable
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
