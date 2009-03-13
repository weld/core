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

/**
 * Helper for XSD related operations
 * 
 * @author Nicklas Karlsson
 *
 */
public class XSDHelper
{
   // The annotation processing environment
   private ProcessingEnvironment processingEnvironment;
   // The cache of already processed classes
   private Map<String, ClassModel> classModelCache = new HashMap<String, ClassModel>();
   // The XSD documents of the affected packages
   private Map<String, Document> packageXSDs = new HashMap<String, Document>();

   /**
    * Creates a new helper
    * 
    * @param processingEnvironment The processing environment
    */
   public XSDHelper(ProcessingEnvironment processingEnvironment)
   {
      this.processingEnvironment = processingEnvironment;
   }

   /**
    * Updates the XSD for the affected packages
    * 
    * @param classModels The list of class models in the batch
    */
   public void updatePackageXSDs(List<ClassModel> classModels)
   {
      for (ClassModel classModel : classModels)
      {
         Document packageXSD = getPackageXSD(classModel.getPackage());
         updateClass(packageXSD, classModel);
      }
   }

   /**
    * Writes the XSD documents back to disk
    */
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

   /**
    * Gets the XSD document for a package
    * 
    * @param packageName The package name of the XSD
    * @return The document
    */
   private Document getPackageXSD(String packageName)
   {
      // Tries to get the document from the cache
      Document packageXSD = packageXSDs.get(packageName);
      if (packageXSD == null)
      {
         // If this is the first modification to a package
         try
         {
            // Read it from disk
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
         // If it was not on disk
         if (packageXSD == null)
         {
            // Create a new document
            packageXSD = createPackageXSD(packageName);
         }
         // And cache it
         packageXSDs.put(packageName, packageXSD);
      }
      return packageXSD;
   }

   /**
    * Updates a package XSD with XSD from a file model
    * 
    * @param packageXSD The package XSD
    * @param classModel The class model
    */
   private void updateClass(Document packageXSD, ClassModel classModel)
   {
      Node oldClassModel = packageXSD.selectSingleNode("//" + classModel.getSimpleName());
      if (oldClassModel != null)
      {
         // Remove the old class definition
         packageXSD.getRootElement().remove(oldClassModel);
      }
      // Create a new one
      packageXSD.getRootElement().addElement(classModel.getSimpleName());
   }

   /**
    * Read the package XSD for a package
    * 
    * @param packageName The package name
    * 
    * @return The document
    * @throws IOException If a file could not be read
    * @throws DocumentException If a document could not be parsed
    */
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

   /**
    * Creates a new XSD document for a package
    * 
    * @param packageName The name of the package
    * @return The document
    */
   private Document createPackageXSD(String packageName)
   {
      Document packageXSD = DocumentHelper.createDocument();

      packageXSD.addElement(new QName("Package", new Namespace(getShortName(packageName), "urn:java:" + packageName)));
      return packageXSD;
   }

   /**
    * Gets the short name of a package (the last part)
    * 
    * @param packageName The package name
    * @return A short name
    */
   private String getShortName(String packageName)
   {
      int lastDot = packageName.lastIndexOf(".");
      return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
   }

   /**
    * Writes a package XSD back to disk
    * 
    * @param packageName The name of the package
    * @param packageXSD The document
    * @throws IOException If the file could not be written
    * @throws SAXException If the document was badly formatted
    */
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

   /**
    * Gets a cached class model 
    * 
    * @param FQN The FQN of the class
    * @return The class model (or null if not cached)
    */
   public ClassModel getCachedClassModel(String FQN)
   {
      return classModelCache.get(FQN);
   }

   /**
    * Puts a class model in the cache
    * 
    * @param classModel The class model
    */
   public void cacheClassModel(ClassModel classModel)
   {
      classModelCache.put(classModel.getName(), classModel);
   }
}
