package org.jboss.webbeans.xml.checker.bean;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface BeanElementChecker
{
   boolean accept(Element beanElement, AnnotatedClass<?> beanClass);
   
   void checkBeanElement(Element beanElement, AnnotatedClass<?> beanClass);
}
