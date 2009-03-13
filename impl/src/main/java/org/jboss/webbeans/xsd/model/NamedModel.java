package org.jboss.webbeans.xsd.model;

public class NamedModel
{
   protected String name;

   public NamedModel()
   {
   }

   public NamedModel(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
}
