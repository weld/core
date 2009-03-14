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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.tools.StandardLocation;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jboss.webbeans.xsd.PackageInfo;
import org.jboss.webbeans.xsd.model.ClassModel;

/**
 * Helper for XSD related operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public class XSDHelper
{
   public static final Set<String> URN_JAVA_EE = new HashSet<String>(Arrays.asList("java.lang", "java.util", "javax.annotation", "javax.inject", "javax.context", "javax.interceptor", "javax.decorator", "javax.event", "javax.ejb", "javax.persistence", "javax.xml.ws", "javax.jms", "javax.sql"));

   // The filed of the annotation processing environment
   private Filer filer;
   // The cache of already processed classes
   private Map<String, ClassModel> classModelCache = new HashMap<String, ClassModel>();
   // The XSD documents of the affected packages
   private Map<String, PackageInfo> packageInfoMap = new HashMap<String, PackageInfo>();

   /**
    * Creates a new helper
    * 
    * @param filer The filer of the processing environment
    */
   public XSDHelper(Filer filer)
   {
      this.filer = filer;
   }

   /**
    * Reads package info
    * 
    * @param packageName The package name 
    * @return The package info of the package
    * @throws DocumentException If the schema could not be parsed
    * @throws IOException If the schema could not be read
    */
   private PackageInfo readPackageInfo(String packageName) throws DocumentException, IOException
   {
      PackageInfo packageInfo = new PackageInfo(packageName);
      packageInfo.setNamespaces(readNamespaces(packageName));
      Document schema = readSchema(packageName);
      packageInfo.setSchema(schema != null ? schema : createSchema(packageName));
      return packageInfo;
   }

   /**
    * Reads the namespaces for a package
    * 
    * @param packageName The name of the package
    * @return The namespaces
    */
   private List<String> readNamespaces(String packageName)
   {
      // TODO dummy
      return new ArrayList<String>();
   }

   /**
    * Creates a new schema document
    * 
    * @param packageName The package name of the schema
    * @return The document
    */
   private Document createSchema(String packageName)
   {
      Document packageXSD = DocumentHelper.createDocument();
      packageXSD.addElement("Package");
      return packageXSD;
   }

   /**
    * Reads a schema for a package
    * 
    * @param packageName The package name
    * @return The schema document
    * @throws DocumentException If the document could not be parsed
    * @throws IOException If the document could not be read
    */
   private Document readSchema(String packageName) throws DocumentException, IOException
   {
      InputStream in = null;
      try
      {
         in = filer.getResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openInputStream();
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
    * Writes package info to the disk
    * 
    * @param packageInfo The package info to store
    */
   private void writePackageInfo(PackageInfo packageInfo)
   {
      try
      {
         writeSchema(packageInfo.getPackageName(), packageInfo.getSchema());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not write schema for " + packageInfo.getPackageName());
      }
      writeNamespaces(packageInfo.getPackageName(), packageInfo.getNamespaces());
   }

   /**
    * Writes the namespaces to disk
    * 
    * @param packageName The package name
    * @param namespaces The namespaces
    */
   private void writeNamespaces(String packageName, List<String> namespaces)
   {
      // TODO dummy
   }

   /**
    * Writes a schema to disk
    * 
    * @param packageName The package name
    * @param schema The schema
    * @throws IOException If the file could not be written 
    */
   private void writeSchema(String packageName, Document schema) throws IOException
   {
      OutputStream out = null;
      try
      {
         OutputFormat format = OutputFormat.createPrettyPrint();
         out = filer.createResource(StandardLocation.CLASS_OUTPUT, packageName, "schema.xsd").openOutputStream();
         XMLWriter writer = new XMLWriter(out, format);
         writer.write(schema);
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
    * Updates the schemas for the affected packages
    * 
    * @param classModels The list of class models in the batch
    */
   public void updateSchemas(List<ClassModel> classModels)
   {
      for (ClassModel classModel : classModels)
      {
         String packageName = classModel.getPackage();
         PackageInfo packageInfo = packageInfoMap.get(packageName);
         if (packageInfo == null) {
            try
            {
               packageInfo = readPackageInfo(packageName);
            }
            catch (DocumentException e)
            {
               throw new RuntimeException("Could not parse schema for package " + packageName);
            }
            catch (IOException e)
            {
               throw new RuntimeException("Could not read schema for package " + packageName);
            }
            packageInfoMap.put(packageName, packageInfo);
         }
         updateClassInSchema(classModel, packageInfo.getSchema());
      }
   }

   /**
    * Writes the schemas back to disk
    */
   public void writeSchemas()
   {
      for (PackageInfo packageInfo : packageInfoMap.values()) {
         writePackageInfo(packageInfo);
      }
   }

   /**
    * Updates a schema with XSD from a file model
    * 
    * @param schema The schema
    * @param classModel The class model
    */
   private void updateClassInSchema(ClassModel classModel, Document schema)
   {
      Node oldClassModel = schema.selectSingleNode("//" + classModel.getSimpleName());
      if (oldClassModel != null)
      {
         // Remove the old class definition
         schema.getRootElement().remove(oldClassModel);
      }
      // Create a new one
      schema.getRootElement().addElement(classModel.getSimpleName());
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
