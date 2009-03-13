package org.jboss.webbeans.xsd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jboss.webbeans.xsd.model.ClassModel;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class AnnotationProcessor extends AbstractProcessor
{
   private Map<String, ClassModel> classModelCache = new HashMap<String, ClassModel>();
   private Map<String, Document> packageXSDs = new HashMap<String, Document>();
   private List<ClassModel> workingSet = new ArrayList<ClassModel>();

   @Override
   public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
   {
      for (Element element : roundEnv.getRootElements())
      {
         workingSet.add(inspectClass(element));
      }
      if (roundEnv.processingOver())
      {
         writeXSD();
      }
      return true;
   }

   private Document getPackageXSD(String packageName)
   {
      Document packageXSD = packageXSDs.get(packageName);
      if (packageXSD == null)
      {
         packageXSD = initPackageXSD(packageName);
         packageXSDs.put(packageName, packageXSD);
      }
      return packageXSD;
   }

   private String getShortName(String packageName)
   {
      int lastDot = packageName.lastIndexOf(".");
      return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
   }

   private Document initPackageXSD(String packageName)
   {
      Document packageXSD = DocumentHelper.createDocument();

      packageXSD.addElement(new QName("Package", new Namespace(getShortName(packageName), "urn:java:" + packageName)));
      return packageXSD;
   }

   private void writeXSD()
   {
      for (ClassModel classModel : workingSet)
      {
         Document packageXSD = getPackageXSD(classModel.getPackage());
         addClass(packageXSD, classModel);
      }
      for (Document document : packageXSDs.values())
      {
         System.out.println(document.asXML());
      }
   }

   private void addClass(Document packageXSD, ClassModel classModel)
   {
      packageXSD.getRootElement().addElement(classModel.getSimpleName());
   }

   private ClassModel inspectClass(Element element)
   {
      TypeElement typeElement = (TypeElement) element;
      ClassModel classModel = new ClassModel();

      if (typeElement.getSuperclass().getKind() != TypeKind.NONE)
      {
         inspectClass(((DeclaredType) typeElement.getSuperclass()).asElement());
      }

      ClassModel parent = classModelCache.get(typeElement.getSuperclass().toString());
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
      classModelCache.put(classModel.getName(), classModel);
      return classModel;
   }

}
