package org.jboss.webbeans.xsd.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
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
import org.jboss.webbeans.xsd.model.ClassModel;
import org.xml.sax.SAXException;

public class XSDHelper
{
   private ProcessingEnvironment processingEnvironment;
   private Map<String, ClassModel> classModelCache = new HashMap<String, ClassModel>();
   private Map<String, Document> packageXSDs = new HashMap<String, Document>();

   public XSDHelper(ProcessingEnvironment processingEnvironment)
   {
      this.processingEnvironment = processingEnvironment;
   }

   public void updatePackageXSDs(List<ClassModel> classModels)
   {
      for (ClassModel classModel : classModels)
      {
         Document packageXSD = getPackageXSD(classModel.getPackage());
         updateClass(packageXSD, classModel);
      }
   }

   public void flushPackageXSDs()
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

   private void updateClass(Document packageXSD, ClassModel classModel)
   {
      Node oldClassModel = packageXSD.selectSingleNode("//" + classModel.getSimpleName());
      if (oldClassModel != null)
      {
         packageXSD.getRootElement().remove(oldClassModel);
      }
      packageXSD.getRootElement().addElement(classModel.getSimpleName());
   }

   private Document readPackageXSD(String packageName) throws IOException, DocumentException
   {
      InputStream in = null;
      try
      {
         in = processingEnvironment.getFiler().getResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openInputStream();
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

   private Document createPackageXSD(String packageName)
   {
      Document packageXSD = DocumentHelper.createDocument();

      packageXSD.addElement(new QName("Package", new Namespace(getShortName(packageName), "urn:java:" + packageName)));
      return packageXSD;
   }

   private String getShortName(String packageName)
   {
      int lastDot = packageName.lastIndexOf(".");
      return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
   }

   private void writePackageXSD(String packageName, Document packageXSD) throws IOException, SAXException
   {
      OutputStream out = null;
      try
      {
         OutputFormat format = OutputFormat.createPrettyPrint();
         out = processingEnvironment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openOutputStream();
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

   public ClassModel getCachedClassModel(String packageName)
   {
      return classModelCache.get(packageName);
   }

   public void setCachedClassModel(String packageName, ClassModel classModel)
   {
      classModelCache.put(packageName, classModel);
   }
}
