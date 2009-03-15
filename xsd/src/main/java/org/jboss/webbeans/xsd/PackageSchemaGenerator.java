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

package org.jboss.webbeans.xsd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import org.jboss.webbeans.xsd.helpers.DataSetter;
import org.jboss.webbeans.xsd.helpers.XSDHelper;
import org.jboss.webbeans.xsd.model.ClassModel;

/**
 * An annotation processor that updates the package-level XSD for the packages
 * that have had their files compiled.
 * 
 * @author Nicklas Karlsson
 * 
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class PackageSchemaGenerator extends AbstractProcessor
{
   // A helper for the XSD operations
   XSDHelper helper;

   @Override
   public synchronized void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);
      helper = new XSDHelper(processingEnv.getFiler());
   }

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      List<ClassModel> workingSet = new ArrayList<ClassModel>();
      // Iterates over the classes compiled, creates a model of the classes and
      // add them to a working set
      for (Element element : roundEnv.getRootElements())
      {
         workingSet.add(inspectClass(element));
      }
      if (!roundEnv.processingOver())
      {
         // Update the package XSDs for the files changed
         helper.updateSchemas(workingSet);
         // And flush the changes to disk
         helper.writeSchemas();
      }
      return false;
   }

   /**
    * Creates a class model from a class element
    * 
    * @param element The element to analyze
    * @return The class model
    */
   private ClassModel inspectClass(Element element)
   {
      TypeElement typeElement = (TypeElement) element;
      ClassModel classModel = new ClassModel();

      // If the class has superclass's, scan them recursively
      if (typeElement.getSuperclass().getKind() != TypeKind.NONE)
      {
         inspectClass(((DeclaredType) typeElement.getSuperclass()).asElement());
      }

      // Gets the parent from the cache. We know it's there since we has scanned
      // the
      // hierarchy already
      ClassModel parent = helper.getCachedClassModel(typeElement.getSuperclass().toString());
      // Populate the class level info (name, parent etc)
      DataSetter.populateClassModel(classModel, element, parent);
      // Filter out the fields and populate the model
      for (Element field : ElementFilter.fieldsIn(element.getEnclosedElements()))
      {
         DataSetter.populateFieldModel(classModel, field);
      }
      // Filter out the methods and populate the model
      for (Element method : ElementFilter.methodsIn(element.getEnclosedElements()))
      {
         DataSetter.populateMethodModel(classModel, method);
      }
      // Filter out the constructors and populate the model
      for (Element constructor : ElementFilter.constructorsIn(element.getEnclosedElements()))
      {
         DataSetter.populateMethodModel(classModel, constructor);
      }
      // Place the new class model in the cache
      helper.cacheClassModel(classModel);
      return classModel;
   }

}
