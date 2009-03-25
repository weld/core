package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.ParseXmlHelper;
import org.jboss.webbeans.xml.XmlConstants;

public class JmsResourceTypeObtainer implements BeanTypeObtainer
{
   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (ParseXmlHelper.isJavaEeNamespace(beanElement) && 
            (beanElement.getName().equalsIgnoreCase(XmlConstants.TOPIC) || 
                  beanElement.getName().equalsIgnoreCase(XmlConstants.QUEUE)))
         return true;
      return false;
   }

   public BeanType obtainType(Element beanElement, AnnotatedClass<?> beanClass)
   {
      return BeanType.JMS_RESOURCE;
   }
}
