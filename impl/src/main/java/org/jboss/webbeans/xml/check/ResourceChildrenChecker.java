package org.jboss.webbeans.xml.check;

import java.util.Iterator;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;

public class ResourceChildrenChecker implements BeanChildrenChecker
{

   public boolean accept(Element element, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(element) && 
            (element.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  element.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return false;
      
      Iterator<?> elIterator = element.elementIterator();
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

   public void checkChildren(Element element)
   {
      // TODO Auto-generated method stub

   }

}
