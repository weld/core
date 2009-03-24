package org.jboss.webbeans.util.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeImpl implements ParameterizedType
{
   private final Type[] actualTypeArguments;
   private final Type rawType;
   private final Type ownerType;

   public ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type ownerType)
   {
      this.actualTypeArguments = actualTypeArguments;
      this.rawType = rawType;
      this.ownerType = ownerType;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public Type getOwnerType()
   {
      return ownerType;
   }

   public Type getRawType()
   {
      return rawType;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(actualTypeArguments);
      result = prime * result + ((ownerType == null) ? 0 : ownerType.hashCode());
      result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ParameterizedType))
         return false;
      
      final ParameterizedType other = (ParameterizedType) obj;
      if (!Arrays.equals(actualTypeArguments, other.getActualTypeArguments()))
         return false;
      if (ownerType == null)
      {
         if (other.getOwnerType() != null)
            return false;
      }
      else if (!ownerType.equals(other.getOwnerType()))
         return false;
      if (rawType == null)
      {
         if (other.getRawType() != null)
            return false;
      }
      else if (!rawType.equals(other.getRawType()))
         return false;
      return true;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(rawType);
      if (actualTypeArguments.length > 0)
      {
         sb.append("<");
         for (Type actualType : actualTypeArguments)
         {
            sb.append(actualType);
            sb.append(",");
         }
         sb.delete(sb.length() - 1, sb.length());
         sb.append(">");
      }
      return sb.toString();
   }
}
