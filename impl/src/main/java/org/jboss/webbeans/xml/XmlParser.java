package org.jboss.webbeans.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Named;
import javax.annotation.Stereotype;
import javax.context.ScopeType;
import javax.inject.DefinitionException;
import javax.inject.DeploymentException;
import javax.inject.DeploymentType;
import javax.interceptor.InterceptorBindingType;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.xml.checker.bean.BeanElementChecker;
import org.jboss.webbeans.xml.checker.bean.ext.JmsResourceElementChecker;
import org.jboss.webbeans.xml.checker.bean.ext.ResourceElementChecker;
import org.jboss.webbeans.xml.checker.bean.ext.SessionBeanElementChecker;
import org.jboss.webbeans.xml.checker.bean.ext.SimpleBeanElementChecker;
import org.jboss.webbeans.xml.checker.beanchildren.ext.NotSimpleBeanChildrenChecker;
import org.jboss.webbeans.xml.checker.beanchildren.ext.SimpleBeanChildrenChecker;

public class XmlParser
{
   private static Log log = Logging.getLog(XmlParser.class);
   
   private final XmlEnvironment environment;
   
   private List<BeanElementChecker> beanElementCheckers = new ArrayList<BeanElementChecker>();
   
   private boolean haveAnyDeployElement = false;
   
   private Map<String, Set<String>> packagesMap = new HashMap<String, Set<String>>();
   
   public XmlParser(XmlEnvironment environment)
   {
      this.environment = environment;      
   }
   
   public void parse()
   {
      for (URL url : environment.getBeansXmlUrls())
      {
         Document document = createDocument(url);
         if (document != null)
         {
            parseForAnnotationTypes(document);
            parseForBeans(document);
            parseForDeploy(document);
         }
      }
   }
   
   private void parseForAnnotationTypes(Document document)
   {
      Element root = document.getRootElement();         
      
      List<Class<? extends Annotation>> bindingTypes = new ArrayList<Class<? extends Annotation>>();
      List<Class<? extends Annotation>> interceptorBindingTypes = new ArrayList<Class<? extends Annotation>>();
      List<Class<? extends Annotation>> stereotypes = new ArrayList<Class<? extends Annotation>>();
      
      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         boolean isBindingType = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.BINDING_TYPE).size() > 0;
         boolean isInterceptorBindingType = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.INTERCEPTOR_BINDING_TYPE).size() > 0;
         boolean isStereotype = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.STEREOTYPE).size() > 0;
         
         if(isBindingType || isInterceptorBindingType || isStereotype)
         {
            Class<? extends Annotation> annotationType = ParseXmlHelper.loadAnnotationClass(element, Annotation.class, environment, packagesMap);
            if(isBindingType)
               bindingTypes.add(annotationType);
            if(isInterceptorBindingType)
            {
               interceptorBindingTypes.add(annotationType);
               checkForInterceptorBindingTypeChildren(element);
            }
            if(isStereotype)
            {
               stereotypes.add(annotationType);
               checkForStereotypeChildren(element);
            }
         }
      }
      ParseXmlHelper.checkForUniqueElements(bindingTypes);
      ParseXmlHelper.checkForUniqueElements(interceptorBindingTypes);
      ParseXmlHelper.checkForUniqueElements(stereotypes);
   }
         
   private void parseForBeans(Document document)
   {
      List<AnnotatedClass<?>> beanClasses = new ArrayList<AnnotatedClass<?>>();
      
      List<Element> beanElements = findBeans(document);      
      for (Element beanElement : beanElements)
      {
         AnnotatedClass<?> beanClass = ParseXmlHelper.loadElementClass(beanElement, Object.class, environment, packagesMap);
         checkBeanElement(beanElement, beanClass);
         checkProduces(beanElement, beanClass);
         beanClasses.add(beanClass);
      }
      
      environment.getClasses().addAll(beanClasses);
   }
   
   private void parseForDeploy(Document document)
   {      
      Element root = document.getRootElement();         
            
      Iterator<?> elIterator = root.elementIterator();
      while (elIterator.hasNext())
      {
         Element element = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(element) && 
               element.getName().equalsIgnoreCase(XmlConstants.DEPLOY))
            environment.getEnabledDeploymentTypes().addAll(obtainDeploymentTypes(element));
      }
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
         fullFillPackagesMap(document);
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
   
   private void checkForInterceptorBindingTypeChildren(Element element)
   {
      Iterator<?> elIterator = element.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element)elIterator.next();
         Class<? extends Annotation> clazz = ParseXmlHelper.loadAnnotationClass(child, Annotation.class, environment, packagesMap);
         if(!clazz.isAnnotationPresent(InterceptorBindingType.class))
            throw new DefinitionException("Direct child <" + child.getName() + "> of interceptor binding type <" + element.getName() + 
                  "> declaration must be interceptor binding type");
         
      }
   }
   
   private void checkForStereotypeChildren(Element stereotypeElement)
   {
      Iterator<?> elIterator = stereotypeElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element stereotypeChild = (Element)elIterator.next();
         Class<? extends Annotation> stereotypeClass = ParseXmlHelper.loadAnnotationClass(stereotypeChild, Annotation.class, environment, packagesMap);
         if(stereotypeClass.isAnnotationPresent(ScopeType.class) || 
               stereotypeClass.isAnnotationPresent(DeploymentType.class) || 
               stereotypeClass.isAnnotationPresent(InterceptorBindingType.class) || 
               stereotypeClass.isAnnotationPresent(Named.class))
            return;
         throw new DefinitionException("Direct child <" + stereotypeChild.getName() + "> of stereotype <" + stereotypeElement.getName() + 
               "> declaration must be scope type, or deployment type, or interceptor binding type, or javax.annotation.Named");
         
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
   private List<Class<? extends Annotation>> obtainDeploymentTypes(Element element)
   {
      if (haveAnyDeployElement)
         throw new DefinitionException("<Deploy> element is specified more than once");

      List<Element> deployElements = element.elements();
      Set<Element> deployElementsSet = new HashSet<Element>(deployElements);
      if(deployElements.size() - deployElementsSet.size() != 0)
         throw new DefinitionException("The same deployment type is declared more than once");
            
      List<Element> standardElements = ParseXmlHelper.findElementsInEeNamespace(element, XmlConstants.STANDARD);
      if (standardElements.size() == 0)
         throw new DeploymentException("The @Standard deployment type must be declared");      
      
      List<Class<? extends Annotation>> deploymentClasses = new ArrayList<Class<? extends Annotation>>();
      List<Element> children = element.elements();
      for (Element child : children)
      {         
         Class<? extends Annotation> deploymentClass = ParseXmlHelper.loadAnnotationClass(child, Annotation.class, environment, packagesMap);
         
         if(!deploymentClass.isAnnotationPresent(DeploymentType.class))
            throw new DefinitionException("<Deploy> child <" + child.getName() + "> must be a deployment type");
                  
         deploymentClasses.add(deploymentClass);
      }
      haveAnyDeployElement = true;
      return deploymentClasses;
   }
   
   private void checkBeanElement(Element beanElement, AnnotatedClass<?> beanClass)
   {
      beanElementCheckers.add(new JmsResourceElementChecker(new NotSimpleBeanChildrenChecker(environment, packagesMap)));
      beanElementCheckers.add(new ResourceElementChecker(new NotSimpleBeanChildrenChecker(environment, packagesMap)));
      beanElementCheckers.add(new SessionBeanElementChecker(new NotSimpleBeanChildrenChecker(environment, packagesMap), environment.getEjbDescriptors()));
      beanElementCheckers.add(new SimpleBeanElementChecker(new SimpleBeanChildrenChecker(environment, packagesMap), environment.getEjbDescriptors()));
      
      boolean isValidType = false;
      for(BeanElementChecker beanElementChecker : beanElementCheckers)
      {
         if(beanElementChecker.accept(beanElement, beanClass))
         {
            beanElementChecker.checkBeanElement(beanElement, beanClass);
            isValidType = true;
            break;
         }
      }
      
      if(!isValidType)
         throw new DefinitionException("Can't determine type of bean element <" + beanElement.getName() + ">");
   }
   
   private void checkProduces(Element beanElement, AnnotatedClass<?> beanClass)
   {//TODO: will refactor
      Iterator<?> beanIterator = beanElement.elementIterator();
      while(beanIterator.hasNext())
      {
         Element beanChild = (Element)beanIterator.next();  
         List<Element> producesElements = ParseXmlHelper.findElementsInEeNamespace(beanChild, XmlConstants.PRODUCES);
         
         if(producesElements.size() == 0)
            continue;
         
         if(producesElements.size() > 1)
            throw new DefinitionException("There is more than one child <Produces> element for <" + beanChild.getName()  + "> element");
         
         List<AnnotatedClass<?>> producesChildTypes = new ArrayList<AnnotatedClass<?>>();
         
         Element producesElement = producesElements.get(0);
         Iterator<?> producesIt = producesElement.elementIterator();
         while(producesIt.hasNext())
         {
            Element producesChild = (Element)producesIt.next();
            AnnotatedClass<?> producesChildClass = ParseXmlHelper.loadElementClass(producesChild, Object.class, environment, packagesMap);
            Class<?> producesChildType = producesChildClass.getRawType();
            boolean isJavaClass = !producesChildType.isEnum() && !producesChildType.isPrimitive() && !producesChildType.isInterface();
            boolean isInterface = producesChildType.isInterface() && !producesChildType.isAnnotation();
            if(isJavaClass || isInterface)
            {
               producesChildTypes.add(producesChildClass);
               continue;
            }
            if(producesChildType.isAnnotation())
            {
               if(producesChildClass.isAnnotationPresent(DeploymentType.class) || 
                     producesChildClass.isAnnotationPresent(ScopeType.class) || 
                     producesChildClass.isAnnotationPresent(Stereotype.class) ||
                     producesChildClass.isAnnotationPresent(Named.class))
                  continue;
                                             
               throw new DefinitionException("<" + producesChild.getName() + "> direct child of <Produces> element for <" + beanChild.getName() 
                     + "> in bean" + beanElement.getName() + "must be DeploymentType or ScopeType or Stereotype or Named");
            }
            throw new DefinitionException("Only Java class, interface type and Java annotation type can be " +
            		"direct child of <Produces> element for <" + beanChild.getName() + "> in bean" + beanElement.getName() + 
            		". Element <" + producesChild.getName() + "> is incorrect");
         }
         
         if(producesChildTypes.size() != 1)
            throw new DefinitionException("More than one or no one child element of <Produces> element for <" + beanChild.getName() + 
                  "> in bean" + beanElement.getName() + " represents a Java class or interface type");
         
         AnnotatedClass<?> expectedType = producesChildTypes.get(0);
                  
         Method beanMethod = null;         
         AnnotatedField<?> beanField = beanClass.getDeclaredField(beanChild.getName(), expectedType);

         try
         {
            List<Class<?>> paramClassesList = new ArrayList<Class<?>>();
            Iterator<?> beanChildIt = beanChild.elementIterator();
            while(beanChildIt.hasNext())
            {
               Element methodChild = (Element)beanChildIt.next();
               if(methodChild.getName().equalsIgnoreCase(XmlConstants.PRODUCES))
                  continue;
               paramClassesList.add(ParseXmlHelper.loadElementClass(methodChild, Object.class, environment, packagesMap).getRawType());
            }
            Class<?>[] paramClasses = (Class<?>[])paramClassesList.toArray(new Class[0]);
            beanMethod = beanClass.getRawType().getDeclaredMethod(beanChild.getName(), paramClasses);
         }
         catch (SecurityException e)
         {}
         catch (NoSuchMethodException e)
         {}
         
         if(beanField != null && beanMethod != null)
            throw new DefinitionException("Class '" + beanClass.getName() + "' has produser field and method with the same name '" + 
                  beanField.getName() + "'");
         
         if(beanField != null)
         {
            if(beanChild.elements().size() > 1)
               throw new DefinitionException("There is more than one direct child element for producer field <" + beanChild.getName() + ">");
            continue;
         }
         
         if(beanMethod != null)
         {
            Iterator<?> beanChildIt = producesElement.elementIterator();
            while(beanChildIt.hasNext())
            {
               Element element = (Element)beanChildIt.next();
               if(!element.getName().equalsIgnoreCase(XmlConstants.PRODUCES) && 
                     ParseXmlHelper.findElementsInEeNamespace(beanChild, XmlConstants.INTERCEPTOR).size() == 0)
                  throw new DefinitionException("Only Produces and interceptor binding types can be direct childs of a producer " +
                  		"method '" + beanChild.getName() + "' declaration in bean '" + beanElement.getName() + "'");
            }
            continue;
         }
         
         throw new DefinitionException("A producer '" + beanChild.getName() + "' doesn't declared in '" + beanElement.getName() + 
               "' class file as method or field");
      }                  
   }
   
   private void fullFillPackagesMap(Document document)
   {
      Element root = document.getRootElement();      
      ParseXmlHelper.checkRootAttributes(root, packagesMap, environment);
      ParseXmlHelper.checkRootDeclaredNamespaces(root, packagesMap, environment);
   }
}
