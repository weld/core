package org.jboss.webbeans.xsd.model;

import java.util.ArrayList;
import java.util.List;

public class ClassModel extends NamedModel
{
   private ClassModel parent;

   private List<ParameterFieldModel> fields = new ArrayList<ParameterFieldModel>();
   private List<MethodModel> methods = new ArrayList<MethodModel>();
   private List<MethodModel> constructors = new ArrayList<MethodModel>();

   public ClassModel()
   {
   }

   public void addField(ParameterFieldModel fieldModel)
   {
      fields.add(fieldModel);
   }

   public void addConstructor(MethodModel constructorModel)
   {
      constructors.add(constructorModel);
   }

   public void addMethod(MethodModel methodModel)
   {
      methods.add(methodModel);
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Name: " + name + "\n");
      buffer.append("Constructors: " + constructors + "\n");
      buffer.append("Methods: " + methods + "\n");
      buffer.append("Fields: " + fields + "\n");
      return buffer.toString();
   }

   public ClassModel getParent()
   {
      return parent;
   }

   public void setParent(ClassModel parent)
   {
      this.parent = parent;
   }

   public String getPackage()
   {
      int lastDot = name.lastIndexOf(".");
      return lastDot < 0 ? name : name.substring(0, lastDot);
   }

   public String getSimpleName()
   {
      int lastDot = name.lastIndexOf(".");
      return lastDot < 0 ? name : name.substring(lastDot + 1);
   }

}
