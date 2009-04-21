package org.jboss.webbeans.xml.registrator.bean.ext;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.checker.beanchildren.BeanChildrenChecker;
import org.jboss.webbeans.xml.registrator.bean.impl.BeanElementRegistratorImpl;

public abstract class NotSimpleBeanElementRegistrator extends BeanElementRegistratorImpl
{
   public NotSimpleBeanElementRegistrator(BeanChildrenChecker childrenChecker)
   {
      super(childrenChecker);
   }

   protected void checkElementDeclaration(Element beanElement, AnnotatedClass<?> beanClass)
   {
      // There is nothing to validate
   }
}
