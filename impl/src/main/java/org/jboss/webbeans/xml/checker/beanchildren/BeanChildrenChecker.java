package org.jboss.webbeans.xml.checker.beanchildren;

import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.xml.XmlEnvironment;

public interface BeanChildrenChecker
{
   void checkChildren(Element beanElement, AnnotatedClass<?> beanClass);

   XmlEnvironment getXmlEnvironment();

   Map<String, Set<String>> getPackagesMap();
}
