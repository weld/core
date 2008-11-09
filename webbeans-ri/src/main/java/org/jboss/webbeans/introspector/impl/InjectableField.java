package org.jboss.webbeans.introspector.impl;

import java.lang.reflect.Field;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.util.Reflections;

/**
 * Abstraction of Java Reflection
 * 
 * @author Pete Muir
 *
 */
public class InjectableField<T> extends Injectable<T, Field>
{
   
   public InjectableField(Field field)
   {
      super(new SimpleAnnotatedField<T>(field));
   }
            
   public InjectableField(AnnotatedField<T> annotatedField)
   {
      super(annotatedField);
   }
   
   public void inject(Object instance, ManagerImpl manager)
   {
      Reflections.setAndWrap(getAnnotatedItem().getDelegate(), instance, getValue(manager));
   }

}
