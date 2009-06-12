/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.MethodSignature;

public class MethodSignatureImpl implements MethodSignature
{
   
   private static final long serialVersionUID = 870948075030895317L;
   
   private final String methodName;
   private final String[] parameterTypes;
   
   public MethodSignatureImpl(WBMethod<?> method)
   {
      this.methodName = method.getName();
      this.parameterTypes = new String[method.getParameters().size()];
      for (int i = 0; i < method.getParameters().size(); i++)
      {
         parameterTypes[i] = method.getParameters().get(i).getRawType().getName();
      }
   }
   
   public MethodSignatureImpl(Method method)
   {
      this.methodName = method.getName();
      this.parameterTypes = new String[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         parameterTypes[i] = method.getParameterTypes()[i].getName();
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