package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.DefinitionException;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.checker.beanchildren.impl.BeanChildrenCheckerImpl;

public class ResourceBeanChildrenChecker extends BeanChildrenCheckerImpl
{
   public ResourceBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }

   public void checkChildren(Element beanElement, AnnotatedClass<?> beanClass)
   {
      List<Element> resourceElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.RESOURCE);
      if(resourceElements.size() > 0)
      {
         checkResourceElements(resourceElements);
         return;
      }
      
      List<Element> persContextElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.PERSISTENCE_CONTEXT);
      if(persContextElements.size() > 0)
      {
         checkPersContextElements(persContextElements);
         return;
      }
         
      List<Element> persUnitElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.PERSISTENCE_UNIT);
      if(persUnitElements.size() > 0)
      {
         checkPersUnitElements(persUnitElements);
         return;
      }
         
      List<Element> ejbElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.EJB);
      if(ejbElements.size() > 0)
      {
         checkEjbElements(ejbElements);
         return;
      }
         
      List<Element> webServiceRefElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.WEB_SERVICE_REF);
      if(webServiceRefElements.size() > 0)
      {
         checkWebServiceRefElements(webServiceRefElements);
         return;
      }         
   }
   
   private void checkResourceElements(List<Element> resourceElements)
   {
      Element resourceElement = resourceElements.get(0);
      
      if(resourceElements.size() > 1)
         throw new DefinitionException("There is more than one <Resource> elements in '" + resourceElement.getParent().getName() + "'");
                  
      List<Element> nameElements = ParseXmlHelper.findElementsInEeNamespace(resourceElement, XmlConstants.NAME);
      List<Element> mappedNameElements = ParseXmlHelper.findElementsInEeNamespace(resourceElement, XmlConstants.MAPPED_NAME);
      
      if(nameElements.size() + mappedNameElements.size() != 1)
         throw new DefinitionException("For a Java EE resource '" + resourceElement.getParent().getName() + "', JNDI name " +
               "or mapped name must be specified using the <name> or <mappedName> child elements of the <Resource> element");
      
      if(nameElements.size() == 1)
         checkNameElementValue(nameElements.get(0));
   }
   
   private void checkPersContextElements(List<Element> persContextElements)
   {
      Element persContextElement = persContextElements.get(0);
      
      if(persContextElements.size() > 1)
         throw new DefinitionException("There is more than one <PersistenceContext> elements in '" + 
               persContextElement.getParent().getName() + "'");
      
      List<Element> unitNameElements = ParseXmlHelper.findElementsInEeNamespace(persContextElement, XmlConstants.UNIT_NAME);
      
      if(unitNameElements.size() != 1)
         throw new DefinitionException("For a persistence context '" + persContextElement.getParent().getName() + "', " +
               "a persistence unit name must be specified using the <unitName> child element of the <PersistenceContext> element");
   }
   
   private void checkPersUnitElements(List<Element> persUnitElements)
   {
      Element persUnitElement = persUnitElements.get(0);
      
      if(persUnitElements.size() > 1)
         throw new DefinitionException("There is more than one <PersistenceUnit> elements in '" + persUnitElement.getParent().getName() + "'");
      
      List<Element> unitNameElements = ParseXmlHelper.findElementsInEeNamespace(persUnitElement, XmlConstants.UNIT_NAME);
      if(unitNameElements.size() != 1)
         throw new DefinitionException("For a persistence unit '" + persUnitElement.getParent().getName() + "', " +
               "a persistence unit name must be specified using the <unitName> child element of the <PersistenceUnit> element");
   }
   
   private void checkEjbElements(List<Element> ejbElements)
   {
      Element ejbElement = ejbElements.get(0);
      
      if(ejbElements.size() > 1)
         throw new DefinitionException("There is more than one <EJB> elements in '" + ejbElement.getParent().getName() + "'");
      
      List<Element> nameElements = ParseXmlHelper.findElementsInEeNamespace(ejbElement, XmlConstants.NAME);
      List<Element> mappedNameElements = ParseXmlHelper.findElementsInEeNamespace(ejbElement, XmlConstants.MAPPED_NAME);
      List<Element> ejbLinkElements = ParseXmlHelper.findElementsInEeNamespace(ejbElement, XmlConstants.EJB_LINK);
      
      if(nameElements.size() + mappedNameElements.size() + ejbLinkElements.size() != 1)
         throw new DefinitionException("For a remote EJB '" + ejbElement.getParent().getName() + "', JNDI name, mapped name or EJB link " +
               "must be specified using the <name>, <mappedName> or <ejbLink> child elements of the <EJB> element");
      
      if(nameElements.size() == 1)
         checkNameElementValue(nameElements.get(0));
   }
   
   private void checkWebServiceRefElements(List<Element> webServiceRefElements)
   {
      Element webServiceRefElement = webServiceRefElements.get(0);
      
      if(webServiceRefElements.size() > 1)
         throw new DefinitionException("There is more than one <WebServiceRef> elements in '" + 
               webServiceRefElement.getParent().getName() + "'");
      
      List<Element> nameElements = ParseXmlHelper.findElementsInEeNamespace(webServiceRefElement, XmlConstants.NAME);
      List<Element> mappedNameElements = ParseXmlHelper.findElementsInEeNamespace(webServiceRefElement, XmlConstants.MAPPED_NAME);
      
      if(nameElements.size() == 0 && mappedNameElements.size() == 0)
         throw new DefinitionException("For a web service '" + webServiceRefElement.getParent().getName() + "', JNDI name " +
               "or mapped name must be specified using the <name> or <mappedName> child elements of the <WebServiceRef> element");
      
      if(nameElements.size() == 1)
         checkNameElementValue(nameElements.get(0));
   }
   
   private void checkNameElementValue(Element nameElement)
   {
      String nameValue = nameElement.getData().toString();
      if(!nameValue.startsWith(XmlConstants.JAVA_GLOBAL) && !nameValue.startsWith(XmlConstants.JAVA_APP))
         throw new DefinitionException("The JNDI name specified by the <name> element in <" + nameElement.getParent().getName() + "> for '" +
         		nameElement.getParent().getParent().getName() + "' must be a name in the global java:global or application java:app naming context");
   }
}
