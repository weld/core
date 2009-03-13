package org.jboss.webbeans.xsd.model;

public class ParameterModel extends FieldModel
{

   public ParameterModel(String name, String type)
   {
      super(name, type);
   }
   
   @Override
   public boolean equals(Object other)
   {
      ParameterModel otherModel = (ParameterModel) other;
      return type.equals(otherModel.getType());
   }

   @Override
   public int hashCode()
   {
      return type.hashCode();
   }   

}
