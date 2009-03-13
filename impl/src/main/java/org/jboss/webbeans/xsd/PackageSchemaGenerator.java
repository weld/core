package org.jboss.webbeans.xsd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import javax.tools.StandardLocation;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jboss.webbeans.xsd.helpers.DataSetter;
import org.jboss.webbeans.xsd.model.ClassModel;
import org.xml.sax.SAXException;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class PackageSchemaGenerator extends AbstractProcessor
{
   private Map<String, ClassModel> classModelCache = new HashMap<String, ClassModel>();
   private Map<String, Document> packageXSDs = new HashMap<String, Document>();

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
         updatePackageXSDs(workingSet);
         flushPackageXSDs();
      }
      return false;
   }

   private Document getPackageXSD(String packageName)
   {
      Document packageXSD = packageXSDs.get(packageName);
      if (packageXSD == null)
      {
         try
         {
            packageXSD = readPackageXSD(packageName);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Could not read schema for package " + packageName);
         }
         catch (DocumentException e)
         {
            throw new RuntimeException("Could not parse schema for package " + packageName);
         }
         if (packageXSD == null)
         {
            packageXSD = createPackageXSD(packageName);
         }
         packageXSDs.put(packageName, packageXSD);
      }
      return packageXSD;
   }

   private Document readPackageXSD(String packageName) throws IOException, DocumentException
   {
      InputStream in = null;
      try
      {
         in = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openInputStream();
         return new SAXReader().read(in);
      }
      catch (IOException e)
      {
         return null;
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   private void flushPackageXSDs()
   {
      for (Entry<String, Document> entry : packageXSDs.entrySet())
      {
         try
         {
            writePackageXSD(entry.getKey(), entry.getValue());
         }
         catch (IOException e)
         {
            throw new RuntimeException("Could not flush XSD for " + entry.getKey());
         }
         catch (SAXException e)
         {
            throw new RuntimeException("Could not parse XSD when flushing for " + entry.getKey());
         }
      }
   }

   private void writePackageXSD(String packageName, Document packageXSD) throws IOException, SAXException
   {
      OutputStream out = null;
      try
      {
         OutputFormat format = OutputFormat.createPrettyPrint();
         out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openOutputStream();
         XMLWriter writer = new XMLWriter(out, format);
         writer.write(packageXSD);
         writer.flush();
         writer.close();
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   private String getShortName(String packageName)
   {
      int lastDot = packageName.lastIndexOf(".");
      return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
   }

   private Document createPackageXSD(String packageName)
   {
      Document packageXSD = DocumentHelper.createDocument();

      packageXSD.addElement(new QName("Package", new Namespace(getShortName(packageName), "urn:java:" + packageName)));
      return packageXSD;
   }

   private void updatePackageXSDs(List<ClassModel> classModels)
   {
      for (ClassModel classModel : classModels)
      {
         Document packageXSD = getPackageXSD(classModel.getPackage());
         updateClass(packageXSD, classModel);
      }
   }

   private void updateClass(Document packageXSD, ClassModel classModel)
   {
      Node oldClassModel = packageXSD.selectSingleNode("//" + classModel.getSimpleName());
      if (oldClassModel != null)
      {
         packageXSD.getRootElement().remove(oldClassModel);
      }
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
