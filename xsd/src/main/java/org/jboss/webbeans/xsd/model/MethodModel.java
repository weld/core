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

package org.jboss.webbeans.xsd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The model of a method
 * 
 * @author Nicklas Karlsson
 *
 */
public class MethodModel extends NamedModel
{
   private String returnType;
   private List<ParameterModel> parameters = new ArrayList<ParameterModel>();

   public MethodModel(String name, String returnType)
   {
      super(name);
      this.returnType = returnType;
   }

   public void addParameter(ParameterModel parameter)
   {
      parameters.add(parameter);
   }

   public List<ParameterModel> getParameters()
   {
      return parameters;
   }

   public String getReturnType()
   {
      return returnType;
   }

   @Override
   public boolean equals(Object other)
   {
      MethodModel otherModel = (MethodModel) other;
      return name.equals(otherModel.getName()) && returnType.equals(otherModel.getReturnType()) && parameters.equals(otherModel.getParameters());
   }

   @Override
   public int hashCode()
   {
      return name.hashCode() + returnType.hashCode() + parameters.hashCode();
   }
   
   @Override
   public String toString()
   {
      return returnType + " " + name + "(" + (parameters.isEmpty() ? "" : parameters) + ")";
   }

}
