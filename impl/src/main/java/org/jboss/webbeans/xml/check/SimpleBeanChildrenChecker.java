package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.XmlConstants;

public class SimpleBeanChildrenChecker implements BeanChildrenChecker
{

   public boolean accept(Element element, AnnotatedClass<?> beanClass)
   {
      ManagerImpl manager = CurrentManager.rootManager();
      boolean isSessionBean = manager.getEjbDescriptorCache().containsKey(element.getName()) || 
                                          element.attribute(XmlConstants.EJB_NAME) != null;
      
      if (!beanClass.isAbstract() && !isSessionBean && !beanClass.isParameterizedType())
         return true;

      return false;
   }

   public void checkChildren(Element element)
   {
      // TODO Auto-generated method stub

   }
}
