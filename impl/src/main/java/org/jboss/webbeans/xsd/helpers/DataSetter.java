package org.jboss.webbeans.xsd.helpers;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.jboss.webbeans.xsd.model.ClassModel;
import org.jboss.webbeans.xsd.model.FieldModel;
import org.jboss.webbeans.xsd.model.MethodModel;
import org.jboss.webbeans.xsd.model.ParameterModel;

public class DataSetter
{

   private static boolean isPublic(Element element)
   {
      return element.getModifiers().contains(Modifier.PUBLIC);
   }

   public static void populateClassModel(ClassModel classModel, Element element, ClassModel parent)
   {
      TypeElement typeElement = (TypeElement) element;
      classModel.setName(typeElement.getQualifiedName().toString());
      classModel.setParent(parent);
   }

   public static void populateFieldModel(ClassModel classModel, Element element)
   {
      if (!isPublic(element))
      {
         return;
      }
      String name = element.getSimpleName().toString();
      String type = element.asType().toString();
      classModel.addField(new FieldModel(name, type));
   }

   public static void populateMethodModel(ClassModel classModel, Element element)
   {
      if (!isPublic(element))
      {
         return;
      }
      ExecutableElement executableElement = (ExecutableElement) element;

      String name = element.getSimpleName().toString();
      String returnType = executableElement.getReturnType().toString();
      MethodModel method = new MethodModel(name, returnType);

      for (VariableElement parameterElement : executableElement.getParameters())
      {
         String paramName = parameterElement.getSimpleName().toString();
         String paramType = parameterElement.asType().toString();
         ParameterModel parameter = new ParameterModel(paramName, paramType);
         method.addParameter(parameter);
      }
      if ("<init>".equals(name))
      {
         classModel.addConstructor(method);
      }
      else
      {
         classModel.addMethod(method);
      }
   }

}
