package org.jboss.webbeans.xml.registrator.bean;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface BeanElementRegistrator
{
   boolean accept(Element beanElement, AnnotatedClass<?> beanClass);
   
   void registerBeanElement(Element beanElement, AnnotatedClass<?> beanClass);
}
