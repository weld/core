package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;

public class JmsResourceChildrenChecker implements BeanChildrenChecker
{
   public boolean accept(Element element, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(element) && 
            (element.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  element.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return true;
      return false;
   }

   public void checkChildren(Element element)
   {
      // TODO Auto-generated method stub

   }

}
