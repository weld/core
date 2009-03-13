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

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class PackageSchemaGenerator extends AbstractProcessor
{
   XSDHelper helper;

   @Override
   public synchronized void init(ProcessingEnvironment processingEnv)
   {
      super.init(processingEnv);
      helper = new XSDHelper(processingEnv);
   }

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      List<ClassModel> workingSet = new ArrayList<ClassModel>();
      for (Element element : roundEnv.getRootElements())
      {
         workingSet.add(inspectClass(element));
      }
      if (!roundEnv.processingOver())
      {
         helper.updatePackageXSDs(workingSet);
         helper.flushPackageXSDs();
      }
      return false;
   }

   private ClassModel inspectClass(Element element)
   {
      TypeElement typeElement = (TypeElement) element;
      ClassModel classModel = new ClassModel();

      if (typeElement.getSuperclass().getKind() != TypeKind.NONE)
      {
         inspectClass(((DeclaredType) typeElement.getSuperclass()).asElement());
      }

      ClassModel parent = helper.getCachedClassModel(typeElement.getSuperclass().toString());
      DataSetter.populateClassModel(classModel, element, parent);
      for (Element field : ElementFilter.fieldsIn(element.getEnclosedElements()))
      {
         DataSetter.populateFieldModel(classModel, field);
      }
      for (Element method : ElementFilter.methodsIn(element.getEnclosedElements()))
      {
         DataSetter.populateMethodModel(classModel, method);
      }
      for (Element constructor : ElementFilter.constructorsIn(element.getEnclosedElements()))
      {
         DataSetter.populateMethodModel(classModel, constructor);
      }
      helper.setCachedClassModel(classModel.getName(), classModel);
      return classModel;
   }


}
