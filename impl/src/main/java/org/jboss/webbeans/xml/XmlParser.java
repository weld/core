package org.jboss.webbeans.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.DefinitionException;
import javax.inject.DeploymentException;
import javax.inject.DeploymentType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;

public class XmlParser
{
   private static Log log = Logging.getLog(XmlParser.class);
   
   private final XmlEnvironment environment;
   
   public XmlParser(XmlEnvironment environment)
   {
      this.environment = environment;
   }
   
   public void parse()
   {
      // TODO extremely inefficient, no need to make dom4j parse each beans.xml multiple times, do as one parse over each document
      parseForBeans();
      parseForDeploy();
   }
         
   private void parseForBeans()
   {

      for (URL url : environment.getBeansXmlUrls())
      {
         Document document = createDocument(url);
         if (document != null)
         {
            List<Element> beanElements = findBeans(document);
            // TODO Only pass in classes here
            //environment.getClasses().addAll(ParseXmlHelper.getBeanItems(beanElements));
         }
      }
      
   }
   
   private void parseForDeploy()
   {
      int counter = 0;
            
      for (URL url : environment.getBeansXmlUrls())
      {
         Document document = createDocument(url);
         if (document != null)
         {
            Element root = document.getRootElement();         
            
            Iterator<?> elIterator = root.elementIterator();
            while (elIterator.hasNext())
            {
               Element element = (Element) elIterator.next();
               if (ParseXmlHelper.isJavaEeNamespace(element) && 
                     element.getName().equalsIgnoreCase(XmlConstants.DEPLOY))
                  environment.getEnabledDeploymentTypes().addAll(obtainDeploymentTypes(element, counter++));
            }        
         }
      }
      
   }
   
   @SuppressWarnings("unchecked")
   public boolean checkNamespaces(Document document)
   {
      //TODO: not finished
      Element root = document.getRootElement();
      List<Namespace> declaredNamespaces = root.declaredNamespaces();
            
      return true;
   }
   
   private Document createDocument(URL url)
   {
      try
      {
         InputStream xmlStream;

         xmlStream = url.openStream();
         if (xmlStream.available() == 0)
         {
            return null;
         }
         SAXReader reader = new SAXReader();
         Document document = reader.read(xmlStream);
         checkNamespaces(document);
         return document;
      }
      catch (IOException e)
      {
         String message = "Can not open stream for " + url;
         log.debug(message, e);
         throw new DeploymentException(message, e);
      }
      catch (DocumentException e)
      {
         String message = "Error during the processing of a DOM4J document for " + url;
         log.debug(message, e);
         throw new DeploymentException(message, e);
      }
   }

   private List<Element> findBeans(Document document)
   {
      List<Element> beans = new ArrayList<Element>();

      Element root = document.getRootElement();

      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         if (checkBeanElementName(element) && 
               checkBeanElementChildrenNames(element))
            beans.add(element);
      }

      return beans;
   }   

   private boolean checkBeanElementName(Element element)
   {
      if (ParseXmlHelper.isJavaEeNamespace(element) && 
            (element.getName().equalsIgnoreCase(XmlConstants.DEPLOY) || 
                  element.getName().equalsIgnoreCase(XmlConstants.INTERCEPTORS) || 
                  element.getName().equalsIgnoreCase(XmlConstants.DECORATORS)))
         return false;
      return true;
   }


   private boolean checkBeanElementChildrenNames(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(child) && 
               (child.getName().equalsIgnoreCase(XmlConstants.BINDING_TYPE) || 
                     child.getName().equalsIgnoreCase(XmlConstants.INTERCEPTOR_BINDING_TYPE) || 
                     child.getName().equalsIgnoreCase(XmlConstants.STEREOTYPE)))
            return false;
      }
      return true;
   }
   
   @SuppressWarnings("unchecked")
   // TODO Make this object orientated
   private List<Class<? extends Annotation>> obtainDeploymentTypes(Element element, int counter)
   {
      if (counter > 1)
         throw new DefinitionException("<Deploy> element is specified more than once");

      List<Element> deployElements = element.elements();
      Set<Element> deployElementsSet = new HashSet<Element>(deployElements);
      if(deployElements.size() - deployElementsSet.size() != 0)
         throw new DefinitionException("The same deployment type is declared more than once");
            
      String standardName = XmlConstants.STANDARD;
      String standardPrefix = "";
      String standardUri = XmlConstants.JAVA_EE_NAMESPACE;
      Namespace standardNamespace = new Namespace(standardPrefix, standardUri);
      
      List<Class<? extends Annotation>> deploymentClasses = new ArrayList<Class<? extends Annotation>>();
      List<Element> children = element.elements();
      for (Element child : children)
      {
         Class<?> deploymentClass;
         
         if (ParseXmlHelper.isJavaEeNamespace(child))
            deploymentClass = loadJavaEeDeploymentType(child);
         else
            deploymentClass = ParseXmlHelper.loadClass(child);
         
         if(!deploymentClass.isAnnotation())
            throw new DeploymentException("<Deploy> child " + element.getName() + " must be a Java annotation type");
         
         if(deploymentClass.getAnnotation(DeploymentType.class) == null)
            throw new DefinitionException("<Deploy> child " + element.getName() + " must be a deployment type");
                  
         deploymentClasses.add(deploymentClass.asSubclass(Annotation.class));
      }
      return deploymentClasses;
   }
   
   private Class<?> loadJavaEeDeploymentType(Element element)
   {
      for(JavaEePackage possiblePackage : JavaEePackage.values())
      {
         String className = possiblePackage + "." + element.getName();
         try
         {
            return Class.forName(className);
         }
         catch (ClassNotFoundException e)
         {}
      }
      throw new DefinitionException("Could not find " + element.getName() + "in the Java EE namespace");
   }
}
