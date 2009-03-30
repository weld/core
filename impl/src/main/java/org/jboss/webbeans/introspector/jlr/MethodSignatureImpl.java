package org.jboss.webbeans.introspector.jlr;

import java.util.Arrays;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.MethodSignature;

public class MethodSignatureImpl implements MethodSignature
{
   
   private final String methodName;
   private final String[] parameterTypes;
   
   public MethodSignatureImpl(AnnotatedMethod<?> method)
   {
      this.methodName = method.getName();
      this.parameterTypes = new String[method.getParameters().size()];
      for (int i = 0; i < method.getParameters().size(); i++)
      {
         parameterTypes[i] = method.getParameters().get(i).getRawType().getName();
      }
      
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof MethodSignatureImpl)
      {
         MethodSignature that = (MethodSignature) obj;
         return this.getMethodName().equals(that.getMethodName()) && Arrays.equals(this.getParameterTypes(), that.getParameterTypes());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      int hashCode = 17;
      hashCode += getMethodName().hashCode() * 5;
      hashCode += getParameterTypes().hashCode() * 7;
      return hashCode;
   }
   
   public String getMethodName()
   {
      return methodName;
   }
   
   public String[] getParameterTypes()
   {
      return parameterTypes;
   }
   
   @Override
   public String toString()
   {
      return getMethodName() + Arrays.asList(getParameterTypes()).toString().replace('[', '(').replace(']', ')');
   }
   
}