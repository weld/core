package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

public class SimpleInjectable<T> extends Injectable<T, Object>
{
   
   public SimpleInjectable(Class<T> type, Annotation[] bindingTypes)
   {
      super(new SimpleAnnotatedItem<T, Object>(bindingTypes, type));
   }

}
