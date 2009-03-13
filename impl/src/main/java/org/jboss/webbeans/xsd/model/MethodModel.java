package org.jboss.webbeans.xsd.model;

import java.util.ArrayList;
import java.util.List;

public class MethodModel extends NamedModel
{
   private String returnType;
   private List<ParameterModel> parameters = new ArrayList<ParameterModel>();

   public MethodModel(String name, String returnType)
   {
      super(name);
      this.returnType = returnType;
   }

   public void addParameter(ParameterModel parameter)
   {
      parameters.add(parameter);
   }

   public List<ParameterModel> getParameters()
   {
      return parameters;
   }

   public String getReturnType()
   {
      return returnType;
   }

   @Override
   public boolean equals(Object other)
   {
      MethodModel otherModel = (MethodModel) other;
      return name.equals(otherModel.getName()) && returnType.equals(otherModel.getReturnType()) && parameters.equals(otherModel.getParameters());
   }

   @Override
   public int hashCode()
   {
      return name.hashCode() + returnType.hashCode() + parameters.hashCode();
   }
   
   @Override
   public String toString()
   {
      return returnType + " " + name + "(" + (parameters.isEmpty() ? "" : parameters) + ")";
   }

}
