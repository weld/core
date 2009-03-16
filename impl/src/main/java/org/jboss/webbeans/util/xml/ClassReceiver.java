package org.jboss.webbeans.util.xml;

import java.lang.reflect.AnnotatedElement;

import org.dom4j.Element;

public interface ClassReceiver
{
   boolean accept(Element element);

   AnnotatedElement reciveClass(Element element);
}
