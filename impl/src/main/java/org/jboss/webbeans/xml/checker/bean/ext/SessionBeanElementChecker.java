package org.jboss.webbeans.xml.checker.bean.ext;

import org.dom4j.Element;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public class SessionBeanElementChecker extends NotSimpleBeanElementChecker
{
   public SessionBeanElementChecker(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      ManagerImpl manager = CurrentManager.rootManager();
      if (manager.getEjbDescriptorCache().containsKey(beanElement.getName()) ||
            beanElement.attribute(XmlConstants.EJB_NAME) != null)
         return true;
      return false;
   }
}
