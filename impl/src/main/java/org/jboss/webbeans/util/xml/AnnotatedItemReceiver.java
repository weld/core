package org.jboss.webbeans.util.xml;


import org.dom4j.Element;
import org.jboss.webbeans.introspector.AnnotatedItem;

public interface AnnotatedItemReceiver
{
   boolean accept(Element element);

   AnnotatedItem<?, ?> receiveAnnotatedItem(Element element);
}
