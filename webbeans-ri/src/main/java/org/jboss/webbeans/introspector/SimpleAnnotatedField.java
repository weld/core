package org.jboss.webbeans.introspector;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class SimpleAnnotatedField<T> extends AbstractAnnotatedMember<T, Field> implements AnnotatedField<T>
{
   
   private Type[] actualTypeArguments = new Type[0];
   
   private Field field;
   
   public SimpleAnnotatedField(Field field)
   {
      super(buildAnnotationMap(field));
      this.field = field;
      if (field.getGenericType() instanceof ParameterizedType)
      {
         ParameterizedType type = (ParameterizedType) field.getGenericType();
         actualTypeArguments = type.getActualTypeArguments();
      }
   }

   public Field getAnnotatedField()
   {
      return field;
   }

   public Field getDelegate()
   {
      return field;
   }
   
   public Class<T> getType()
   {
      return (Class<T>) field.getType();
   }
   
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

}
