package org.jboss.webbeans.xml.checker.beanchildren.ext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.XmlEnvironment;

public class JmsResourceBeanChildrenChecker extends ResourceBeanChildrenChecker
{
   private static final String RESOURCE_TYPE = "JMS";
   
   public JmsResourceBeanChildrenChecker(XmlEnvironment environment, Map<String, Set<String>> packagesMap)
   {
      super(environment, packagesMap);
   }
   
   protected void checkRIBean(Element beanElement, AnnotatedClass<?> beanClass)
   {
      List<Element> resourceElements = ParseXmlHelper.findElementsInEeNamespace(beanElement, XmlConstants.RESOURCE);
      if (resourceElements.size() > 0)
         checkResourceElements(resourceElements);
   }
   
   protected String getResourceType()
   {
      return RESOURCE_TYPE;
   }
}
