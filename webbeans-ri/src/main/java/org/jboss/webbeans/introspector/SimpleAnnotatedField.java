package org.jboss.webbeans.introspector;

import java.lang.reflect.Field;

public class SimpleAnnotatedField<T> extends AbstractAnnotatedItem<T, Field> implements AnnotatedField<T>
{
   
   private Field field;
   
   public SimpleAnnotatedField(Field field)
   {
      super(buildAnnotationMap(field));
      this.field = field;
   }

   public Field getAnnotatedField()
   {
      return field;
   }
   
   @Override
   public String toString()
   {
      return field + " " + getAnnotatedField().toString();
   }

   public Field getDelegate()
   {
      return field;
   }
   
   public Class<T> getType()
   {
      return (Class<T>) field.getType();
   }

}
