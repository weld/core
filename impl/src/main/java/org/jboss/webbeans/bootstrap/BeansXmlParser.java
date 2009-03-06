package org.jboss.webbeans.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.DefinitionException;
import javax.inject.DeploymentException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.dom.NodeListIterable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Temporary XML parser to get essential data (like enabled deployment types)
 * until we have a full XML parser/binder
 * 
 * @author Pete Muir
 *
 */
public class BeansXmlParser
{
   
   private static class DeployElement
   {
      private URL file;
      private Element element;
      private Map<String, String> namespaces;
      
      public DeployElement(URL file, Element element, Map<String, String> namespaces)
      {
         super();
         this.file = file;
         this.element = element;
         this.namespaces = namespaces;
      }
      
      public URL getFile()
      {
         return file;
      }
      
      public Element getElement()
      {
         return element;
      }
      
      public Map<String, String> getNamespaces()
      {
         return namespaces;
      }
      
      @Override
      public String toString()
      {
         return "File: " + getFile() + "; Node: " + getElement();
      }
      
   }
   
   public static final String EE_NAMESPACE = "urn:java:ee";
   
   private final Iterable<URL> beansXml;
   private final ResourceLoader resourceLoader;
   
   private List<Class<? extends Annotation>> enabledDeploymentTypes;
   
   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return enabledDeploymentTypes;
   }
   
   public BeansXmlParser(ResourceLoader resourceLoader, Iterable<URL> beansXml)
   {
      this.beansXml = beansXml;
      this.resourceLoader = resourceLoader;
   }
   
   public void parse()
   {
      DocumentBuilder documentBuilder;
      try
      {
         documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         throw new DeploymentException("Error configuring XML parser", e);
      }
      List<DeployElement> deployElements = new ArrayList<DeployElement>(); 
      for (URL url : beansXml)
      {
         InputStream is;
         boolean fileHasContents;
         try
         {
            is = url.openStream();
            fileHasContents = is.available() > 0;
         }
         catch (IOException e)
         {
            throw new DeploymentException("Error loading beans.xml " + url.toString(), e);
         }
         if (fileHasContents)
         {
            Document document;
            try
            {
               document = documentBuilder.parse(is);
               document.normalize();
            }
            catch (SAXException e)
            {
               throw new DeploymentException("Error parsing beans.xml " + url.toString(), e);
            }
            catch (IOException e)
            {
               throw new DeploymentException("Error loading beans.xml " + url.toString(), e);
            }
            Element beans = document.getDocumentElement();
            Map<String, String> namespaces = new HashMap<String, String>();
            for (int i = 0; i < beans.getAttributes().getLength(); i++)
            {
               Node child = beans.getAttributes().item(i);
               if (child instanceof Attr)
               {
                  Attr attr = (Attr) child;
                  if (attr.getName().startsWith("xmlns"))
                  {
                     String namespacePrefix;
                     if (attr.getName().length() == 5)
                     {
                        namespacePrefix = "";
                     }
                     else
                     {
                        namespacePrefix = attr.getName().substring(6);
                     }
                     
                     String namespace = attr.getValue();
                     namespaces.put(namespacePrefix, namespace);
                  }
               }
            }
            for (Node child : new NodeListIterable(beans.getChildNodes()))
            {
               if (child instanceof Element && "Deploy".equals(child.getNodeName()))
               {
                  deployElements.add(new DeployElement(url, (Element) child, namespaces));
               }
            }
         }
      }
      if (deployElements.size() > 1)
      {
         throw new DeploymentException("<Deploy> can only be specified once, but it is specified muliple times " + deployElements);
      }
      else if (deployElements.size() == 1)
      {
         DeployElement deployElement = deployElements.get(0);
         enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
         for (Node child : new NodeListIterable(deployElement.getElement().getChildNodes()))
         {
            if (child instanceof Element)
            {
               String className = getAsClassName(child.getNodeName(), deployElement.getNamespaces(), deployElement.getFile());
               if (className != null)
               {
                  enabledDeploymentTypes.add(resourceLoader.classForName(className).asSubclass(Annotation.class));
               }
            }
         }
      }
   }
   
   private static String getAsClassName(String nodeName, Map<String, String> namespaces, URL file)
   {
      String namespacePrefix;
      String simpleClassName;
      if (nodeName.contains(":"))
      {
          namespacePrefix = nodeName.substring(0, nodeName.indexOf(":"));
          simpleClassName = nodeName.substring(nodeName.indexOf(":") + 1);
      }
      else
      {
         namespacePrefix = "";
         simpleClassName = nodeName;
      }
      String namespace = namespaces.get(namespacePrefix);
      if (namespace == null)
      {
         throw new DefinitionException("Prefix " + namespacePrefix + " has no namespace mapped in " + file.getPath());
      }
      String packageName;
      if (namespace.startsWith("urn:java:ee"))
      {
         // Hack for now to return the correct package for this composite package deployment types
         packageName = "javax.inject";
      }
      else if (namespace.startsWith("urn:java:"))
      {
         packageName = namespace.substring(9);
      }
      else
      {
         // Not a Java package
         return null;
      }
      return packageName + "." + simpleClassName;
   }
   
}
