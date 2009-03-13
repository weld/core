/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Helper for examining classes and members and populating the model
 * 
 * @author Nicklas Karlsson
 *
 */
public class DataSetter
{

   /**
    * Checks if an element is public
    * 
    * @param element The element to check
    * @return True if public, false otherwise
    */
   private static boolean isPublic(Element element)
   {
      return element.getModifiers().contains(Modifier.PUBLIC);
   }

   /**
    * Inspects a type element and populates a class model
    *  
    * @param classModel The class model to populate
    * @param element The element to inspect
    * @param parent The parent of the class
    */
   public static void populateClassModel(ClassModel classModel, Element element, ClassModel parent)
   {
      TypeElement typeElement = (TypeElement) element;
      classModel.setName(typeElement.getQualifiedName().toString());
      classModel.setParent(parent);
   }

   /**
    * Inspects a field element and populates a class model
    * 
    * @param classModel The class model to populate
    * @param element The element to inspect
    */
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

   /**
    * Inspects a method or constructor and populates a class model
    * 
    * @param classModel The class model to populate
    * @param element The element to inspect
    */
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
      // OK, checting a little with a common model for methods and constructors
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
