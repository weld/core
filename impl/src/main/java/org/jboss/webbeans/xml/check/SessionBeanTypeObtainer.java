package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.XmlConstants;

public class SessionBeanTypeObtainer implements BeanTypeObtainer
{

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      ManagerImpl manager = CurrentManager.rootManager();
      if (manager.getEjbDescriptorCache().containsKey(beanElement.getName()) ||
            beanElement.attribute(XmlConstants.EJB_NAME) != null)
         return true;
      return false;
   }

   public BeanType obtainType(Element beanElement, AnnotatedClass<?> beanClass)
   {
      return BeanType.SESSION_BEAN;
   }
}
