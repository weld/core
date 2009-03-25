package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface BeanChildrenChecker
{
   boolean accept(Element element, AnnotatedClass<?> beanClass);
   
   void checkChildren(Element element);
}
