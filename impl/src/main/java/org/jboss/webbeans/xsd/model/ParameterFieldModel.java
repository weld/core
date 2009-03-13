package org.jboss.webbeans.xsd.model;

public class ParameterFieldModel extends NamedModel
{
   private String type;

   public ParameterFieldModel(String name, String type)
   {
      super(name);
      this.type = type;
   }
   
   public String getType()
   {
      return type;
   }

}
