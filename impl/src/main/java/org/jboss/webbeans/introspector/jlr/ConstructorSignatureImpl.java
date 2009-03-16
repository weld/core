package org.jboss.webbeans.introspector.jlr;

import java.util.Arrays;

import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.ConstructorSignature;
import org.jboss.webbeans.introspector.MethodSignature;

public class ConstructorSignatureImpl implements ConstructorSignature
{
   
   private final String[] parameterTypes;
   
   public ConstructorSignatureImpl(AnnotatedConstructor<?> method)
   {
      this.parameterTypes = new String[method.getParameters().size()];
      for (int i = 0; i < method.getParameters().size(); i++)
      {
         parameterTypes[i] = method.getParameters().get(i).getRawType().getName();
      }
      
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ConstructorSignatureImpl)
      {
         MethodSignature that = (MethodSignature) obj;
         return Arrays.equals(this.getParameterTypes(), that.getParameterTypes());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
     return getParameterTypes().hashCode();
   }
   
   public String[] getParameterTypes()
   {
      return parameterTypes;
   }
   
}