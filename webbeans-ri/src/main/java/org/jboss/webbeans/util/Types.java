package org.jboss.webbeans.util;

public class Types
{
   
   public static Class<?> boxedType(Class<?> type)
   {
      if (type.isPrimitive())
      {
         if (type.equals(Boolean.TYPE))
         {
            return Boolean.class;
         }
         else if (type.equals(Character.TYPE))
         {
            return Character.class;
         }
         else if (type.equals(Byte.TYPE))
         {
            return Byte.class;
         }
         else if (type.equals(Short.TYPE))
         {
            return Short.class;
         }
         else if (type.equals(Integer.TYPE))
         {
            return Integer.class;
         }
         else if (type.equals(Long.TYPE))
         {
            return Long.class;
         }
         else if (type.equals(Float.TYPE))
         {
            return Float.class;
         }
         else if (type.equals(Double.TYPE))
         {
            return Double.class;
         }
         else 
         {
            throw new IllegalStateException("Some weird type!!!");
         }
      }
      else
      {
         return type;
      }
   }
   
}
