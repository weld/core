package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

public class SimpleInjectable<T> extends Injectable<T, Object>
{
   
   public SimpleInjectable(Class<T> type, Annotation[] bindingTypes)
   {
      super(new SimpleAnnotatedItem<T, Object>(bindingTypes, type));
   }
   
   public SimpleInjectable(Class<T> type, Annotation[] bindingTypes, Type ... actualTypeArguements)
   {
      super(new SimpleAnnotatedItem<T, Object>(bindingTypes, type, actualTypeArguements));
   }
   
   public SimpleInjectable(TypeLiteral<T> apiType, Annotation[] bindingTypes)
   {
      super(new SimpleAnnotatedItem<T, Object>(bindingTypes, apiType));
   }

}
