package org.jboss.webbeans.xml.checker.beanchildren;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface BeanChildrenChecker
{   
   void checkChildren(Element beanElement, AnnotatedClass<?> beanClass);
}
