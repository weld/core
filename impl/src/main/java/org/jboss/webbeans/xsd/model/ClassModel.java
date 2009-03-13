package org.jboss.webbeans.xsd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassModel extends NamedModel
{
   private ClassModel parent;

   private List<FieldModel> fields = new ArrayList<FieldModel>();
   private List<MethodModel> methods = new ArrayList<MethodModel>();
   private List<MethodModel> constructors = new ArrayList<MethodModel>();

   public ClassModel()
   {
   }

   public void addField(FieldModel fieldModel)
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
      buffer.append("Constructors: " + getMergedConstructors() + "\n");
      buffer.append("Methods: " + getMergedMethods() + "\n");
      buffer.append("Fields: " + getMergedFields() + "\n");
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

   public Set<MethodModel> getMergedConstructors()
   {
      return new HashSet<MethodModel>(constructors);
   }

   public List<FieldModel> getFields()
   {
      return fields;
   }

   public Set<FieldModel> getMergedFields()
   {
      Set<FieldModel> mergedFields = new HashSet<FieldModel>(fields);
      ClassModel currentParent = parent;
      while (currentParent != null)
      {
         mergedFields.addAll(currentParent.getFields());
         currentParent = currentParent.getParent();
      }
      return mergedFields;
   }

   public List<MethodModel> getMethods()
   {
      return methods;
   }

   public Set<MethodModel> getMergedMethods()
   {
      Set<MethodModel> mergedMethods = new HashSet<MethodModel>(methods);
      ClassModel currentParent = parent;
      while (currentParent != null)
      {
         mergedMethods.addAll(currentParent.getMethods());
         currentParent = currentParent.getParent();
      }
      return mergedMethods;

   }

}
