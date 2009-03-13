package org.jboss.webbeans.xsd.model;

import java.util.ArrayList;
import java.util.List;

public class MethodModel extends NamedModel
{
   private String returnType;
   private List<ParameterFieldModel> parameters = new ArrayList<ParameterFieldModel>();

   public MethodModel(String name, String returnType)
   {
      super(name);
      this.returnType = returnType;
   }

   public void addParameter(ParameterFieldModel parameter)
   {
      parameters.add(parameter);
   }

   public List<ParameterFieldModel> getParameters()
   {
      return parameters;
   }

   public String getReturnType()
   {
      return returnType;
   }

}
