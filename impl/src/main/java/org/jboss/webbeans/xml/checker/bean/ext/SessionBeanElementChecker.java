package org.jboss.webbeans.xml.checker.bean.ext;

import org.dom4j.Element;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.RootManager;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.XmlConstants;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public class SessionBeanElementChecker extends NotSimpleBeanElementChecker
{

   private final EjbDescriptorCache ejbDescriptors;
	
   public SessionBeanElementChecker(BeanChildrenChecker childrenChecker, EjbDescriptorCache ejbDescriptors)
   {
      super(childrenChecker);
      this.ejbDescriptors = ejbDescriptors;
   }

   public boolean accept(Element beanElement, AnnotatedClass<?> beanClass)
   {
      if (ejbDescriptors.containsKey(beanElement.getName()) ||
            beanElement.attribute(XmlConstants.EJB_NAME) != null)
      {
         return true;
      }
      return false;
   }
}
