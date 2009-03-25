package org.jboss.webbeans.xml.check;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;

public interface BeanTypeObtainer
{
   boolean accept(Element beanElement, AnnotatedClass<?> beanClass);
   
   BeanType obtainType(Element beanElement, AnnotatedClass<?> beanClass);
}
