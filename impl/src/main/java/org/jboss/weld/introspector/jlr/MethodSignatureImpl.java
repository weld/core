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
package org.jboss.weld.introspector.jlr;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.util.collections.Arrays2;

public class MethodSignatureImpl implements MethodSignature
{
   
   private static final long serialVersionUID = 870948075030895317L;
   
   private final String methodName;
   private final String[] parameterTypes;
   
   public MethodSignatureImpl(WeldMethod<?, ?> method)
   {
      this.methodName = method.getName();
      this.parameterTypes = new String[method.getWeldParameters().size()];
      for (int i = 0; i < method.getWeldParameters().size(); i++)
      {
         parameterTypes[i] = method.getWeldParameters().get(i).getJavaClass().getName();
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
      hashCode += methodName.hashCode() * 5;
      hashCode += Arrays.hashCode(parameterTypes) * 7;
      return hashCode;
   }
   
   public String getMethodName()
   {
      return methodName;
   }
   
   public String[] getParameterTypes()
   {
      return Arrays2.copyOf(parameterTypes, parameterTypes.length);
   }
   
   @Override
   public String toString()
   {
      return getMethodName() + Arrays.asList(getParameterTypes()).toString().replace('[', '(').replace(']', ')');
   }
   
}