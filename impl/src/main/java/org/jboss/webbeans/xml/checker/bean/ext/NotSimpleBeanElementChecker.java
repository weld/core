package org.jboss.webbeans.xml.checker.bean.ext;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.checker.bean.impl.BeanElementCheckerImpl;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;

public abstract class NotSimpleBeanElementChecker extends BeanElementCheckerImpl
{
   public NotSimpleBeanElementChecker(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }
   
   public void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass)
   {
      // There is nothing to validate      
   }
}
