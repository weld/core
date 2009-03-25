package org.jboss.webbeans.xml.check;

import java.util.Iterator;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;

public class ResourceTypeObtainer implements BeanTypeObtainer
{

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(beanElement) && 
            (beanElement.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  beanElement.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return false;
      
      Iterator<?> elIterator = beanElement.elementIterator();
      while (elIterator.hasNext())
      {
         Element child = (Element) elIterator.next();
         if (ParseXmlHelper.isJavaEeNamespace(child) && 
               (child.getName().equalsIgnoreCase(XmlConstants.RESOURCE) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_CONTEXT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.PERSISTENCE_UNIT) || 
                     child.getName().equalsIgnoreCase(XmlConstants.EJB) || 
                     child.getName().equalsIgnoreCase(XmlConstants.WEB_SERVICE_REF)))
            return true;
      }
      return false;
   }

   public BeanType obtainType(Element beanElement, AnnotatedClass<?> beanClass)
   {
      return BeanType.RESOURCE;
   }
}
