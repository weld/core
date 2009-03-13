package org.jboss.webbeans.xsd.model;

public class FieldModel extends NamedModel
{
   protected String type;

   public FieldModel(String name, String type)
   {
      super(name);
      this.type = type;
   }

   public String getType()
   {
      return type;
   }

   @Override
   public boolean equals(Object other)
   {
      FieldModel otherModel = (FieldModel) other;
      return name.equals(otherModel.getName()) && type.equals(otherModel.getType());
   }

   @Override
   public int hashCode()
   {
      return name.hashCode() + type.hashCode();
   }

}
