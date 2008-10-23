package org.jboss.webbeans.injectable;

import java.lang.reflect.Field;

import org.jboss.webbeans.introspector.SimpleAnnotatedField;

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

}
