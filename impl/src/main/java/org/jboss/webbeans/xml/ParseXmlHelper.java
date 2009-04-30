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
package org.jboss.webbeans.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.xml.sax.SAXException;

public class ParseXmlHelper
{
   public static boolean isJavaEeNamespace(Element element)
   {
      return XmlConstants.JAVA_EE_NAMESPACE.equalsIgnoreCase(element.getNamespace().getURI());
   }

   public static <T> AnnotatedClass<? extends T> loadElementClass(Element element, Class<T> expectedType, XmlEnvironment environment, 
                                                                     Map<String, Set<String>> packagesMap)
   {
      List<AnnotatedClass<? extends T>> classesList = tryLoadElementClass(element, expectedType, environment, packagesMap);
      String className = element.getName();

      if (classesList.size() == 0)
         throw new DefinitionException("Could not find '" + className + "'");

      return classesList.get(0);
   }

   public static <T> List<AnnotatedClass<? extends T>> tryLoadElementClass(Element element, Class<T> expectedType, XmlEnvironment environment, 
                                                                              Map<String, Set<String>> packagesMap)
   {
      List<AnnotatedClass<? extends T>> classesList = new ArrayList<AnnotatedClass<? extends T>>();
      String prefix = element.getNamespacePrefix();

      for (Map.Entry<String, Set<String>> packagesEntry : packagesMap.entrySet())
      {
         if (packagesEntry.getKey().equalsIgnoreCase(prefix))
         {
            Set<String> packages = packagesEntry.getValue();
            for (String packageName : packages)
            {
               String classPath = packageName + "." + element.getName();
               try
               {
                  AnnotatedClass<? extends T> classType = environment.loadClass(classPath, expectedType);
                  classesList.add(classType);
               }
               catch (ResourceLoadingException e)
               {
               }
            }
         }
      }

      if (classesList.size() > 1)
         throw new DefinitionException("There are multiple packages containing a Java type with the same name '" + element.getName() + "'");

      return classesList;
   }

   public static <T extends Annotation> Class<? extends T> loadAnnotationClass(Element element, Class<T> expectedType, XmlEnvironment environment, 
                                                                                 Map<String, Set<String>> packagesMap)
   {
      List<Class<? extends T>> classesList = new ArrayList<Class<? extends T>>();
      String className = element.getName();
      String prefix = element.getNamespacePrefix();

      for (Map.Entry<String, Set<String>> packagesEntry : packagesMap.entrySet())
      {
         if (packagesEntry.getKey().equalsIgnoreCase(prefix))
         {
            Set<String> packages = packagesEntry.getValue();
            for (String packageName : packages)
            {
               String classPath = packageName + "." + element.getName();
               try
               {
                  classesList.add(environment.loadAnnotation(classPath, expectedType));
               }
               catch (ResourceLoadingException e)
               {
                  // work with this when 'classesList.size() == 0'
               }
               catch (ClassCastException e)
               {
                  throw new DefinitionException("<" + element.getName() + "> must be a Java annotation type");
               }
            }
         }
      }

      if (classesList.size() == 0)
         throw new DefinitionException("Could not find '" + className + "'");

      if (classesList.size() == 1)
         return classesList.get(0);

      throw new DefinitionException("There are multiple packages containing a Java type with the same name '" + className + "'");
   }

   public static void checkRootAttributes(Element root, Map<String, Set<String>> packagesMap, XmlEnvironment environment, URL xmlUrl, Set<URL> schemas)
   {
      Iterator<?> rootAttrIterator = root.attributeIterator();
      while (rootAttrIterator.hasNext())
      {
         Set<String> packagesSet = new HashSet<String>();
         Attribute attribute = (Attribute) rootAttrIterator.next();
         String attrPrefix = attribute.getNamespacePrefix();
         String attrData = attribute.getStringValue();

         String urn = "";
         for (String attrVal : attrData.split(" "))
         {
            if (attrVal.startsWith(XmlConstants.URN_PREFIX))
            {
               urn = attrVal;
               URL namespaceFile = environment.loadFileByUrn(urn, XmlConstants.NAMESPACE_FILE_NAME);

               if (namespaceFile != null)
               {
                  packagesSet.addAll(parseNamespaceFile(namespaceFile));
               }
               else
               {
                  String packageName = urn.replaceFirst(XmlConstants.URN_PREFIX, "");
                  packagesSet.add(packageName);
               }
            }
            if (XmlConstants.SCHEMA_LOCATION.equalsIgnoreCase(attribute.getName()) && 
                  attrVal.startsWith(XmlConstants.HTTP_PREFIX) && urn.trim().length() > 0)
            {
               URL schemaUrl = environment.loadFileByUrn(urn, XmlConstants.SCHEMA_FILE_NAME);
               if (schemaUrl == null)
                  throw new DefinitionException("Could not find '" + XmlConstants.SCHEMA_FILE_NAME + 
                        "' file according to specified URN '" + urn + "'");
               schemas.add(schemaUrl);
            }
         }

         addElementToPackagesMap(packagesMap, attrPrefix, packagesSet);
      }
   }

   public static void checkRootDeclaredNamespaces(Element root, Map<String, Set<String>> packagesMap, XmlEnvironment environment, URL xmlUrl, Set<URL> schemas)
   {
      Iterator<?> namespacesIterator = root.declaredNamespaces().iterator();
      while (namespacesIterator.hasNext())
      {
         Namespace namespace = (Namespace) namespacesIterator.next();
         String prefix = namespace.getPrefix();
         String uri = namespace.getURI();

         if (uri.startsWith(XmlConstants.URN_PREFIX))
         {
            Set<String> packagesSet = new HashSet<String>();

            URL schemaUrl = environment.loadFileByUrn(uri, XmlConstants.SCHEMA_FILE_NAME);
            if (schemaUrl != null)
               schemas.add(schemaUrl);

            URL namespaceFile = environment.loadFileByUrn(uri, XmlConstants.NAMESPACE_FILE_NAME);
            if (namespaceFile != null)
            {
               packagesSet.addAll(parseNamespaceFile(namespaceFile));
            }
            else
            {
               String packageName = uri.replaceFirst(XmlConstants.URN_PREFIX, "");
               packagesSet.add(packageName);
            }

            addElementToPackagesMap(packagesMap, prefix, packagesSet);
         }
      }
   }

   public static void validateXmlWithXsd(URL xmlUrl, Set<URL> schemas)
   {
      try
      {    
         List<InputStream> schemaStreams = new ArrayList<InputStream>();
         for (URL schema : schemas)
         {
            schemaStreams.add(schema.openStream());
         }
         
         SAXReader reader = new SAXReader(true);         
         reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
         reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaStreams.toArray());
         reader.read(xmlUrl.openStream());
      }
      catch (SAXException e)
      {
         String message = "SAXException while validate " + xmlUrl + " with " + schemas;
         throw new DefinitionException(message, e);
      }
      catch (IOException e)
      {
         String message = "IOException while validate " + xmlUrl + " with " + schemas;
         throw new DefinitionException(message, e);
      }
      catch (DocumentException e)
      {
         String message = "DocumentException while validate " + xmlUrl + " with " + schemas;
         throw new DefinitionException(message, e);
      }
   }

   public static List<Element> findElementsInEeNamespace(Element elementParent, String elementName)
   {
      String elementPrefix = "";
      String elementUri = XmlConstants.JAVA_EE_NAMESPACE;

      return findElements(elementParent, elementName, elementPrefix, elementUri);
   }

   public static List<Element> findElements(Element elementParent, String elementName, String elementPrefix, String elementUri)
   {
      List<Element> elements = new ArrayList<Element>();
      Namespace elementNamespace = new Namespace(elementPrefix, elementUri);
      QName qName = new QName(elementName, elementNamespace);
      Iterator<?> elementIterator = elementParent.elementIterator(qName);
      while (elementIterator.hasNext())
      {
         Element element = (Element) elementIterator.next();
         elements.add(element);
      }

      return elements;
   }

   private static Set<String> parseNamespaceFile(URL namespaceFile)
   {
      Set<String> packages = new HashSet<String>();
      Scanner fileScanner;
      try
      {
         fileScanner = new Scanner(namespaceFile.openStream());
         while (fileScanner.hasNextLine())
         {
            String line = fileScanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter(XmlConstants.NAMESPACE_FILE_DELIMETER);
            while (lineScanner.hasNext())
            {
               packages.add(lineScanner.next());
            }
            lineScanner.close();
         }
         fileScanner.close();
         return packages;
      }
      catch (IOException e)
      {
         throw new RuntimeException("Error opening " + namespaceFile.toString());
      }
   }

   private static void addElementToPackagesMap(Map<String, Set<String>> packagesMap, String prefix, Set<String> packagesSet)
   {
      if (packagesMap.containsKey(prefix))
      {
         Set<String> packages = packagesMap.get(prefix);
         packages.addAll(packagesSet);
      }
      else
      {
         packagesMap.put(prefix, packagesSet);
      }
   }

   public static void checkForUniqueElements(List<Class<? extends Annotation>> list)
   {
      Set<Class<? extends Annotation>> set = new HashSet<Class<? extends Annotation>>(list);
      if (list.size() != set.size())
         throw new DefinitionException("A certain annotation type is declared more than once as a binding type, " + 
               "interceptor binding type or stereotype using XML");
   }
}
