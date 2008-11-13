package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;

public class AnnotatedFieldImpl<T> extends AbstractAnnotatedMember<T, Field> implements AnnotatedField<T>
{
   
   private Type[] actualTypeArguments = new Type[0];
   
   private Field field;
   private AnnotatedType<?> declaringClass;
   
   public AnnotatedFieldImpl(Field field, AnnotatedType<?> declaringClass)
   {
      super(buildAnnotationMap(field));
      this.field = field;
      this.declaringClass = declaringClass;
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

   public void inject(Object instance, ManagerImpl manager)
   {
      Reflections.setAndWrap(getDelegate(), instance, getValue(manager));
   }
   
   public String getPropertyName()
   {
      return getName();
   }
   
   public AnnotatedType<?> getDeclaringClass()
   {
      return declaringClass;
   }

}
