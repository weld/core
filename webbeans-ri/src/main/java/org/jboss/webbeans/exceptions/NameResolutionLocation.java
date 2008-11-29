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

package org.jboss.webbeans.exceptions;

/**
 * Exception location info for name resolution exceptions
 * 
 * @author Pete Muir
 */
public class NameResolutionLocation extends Location
{
   // The target of the failure
   private String target;
   
   /**
    * Constructor
    * 
    * @param target The target of the failure 
    */
   public NameResolutionLocation(String target)
   {
      super("Named Based Resolution", null, null);
      
   }
   
   /**
    * Gets the target
    * 
    * @return The target
    */
   public String getTarget()
   {
      return target;
   }
   
   /** 
    * Sets the target
    * 
    * @param target The target
    */
   public void setTarget(String target)
   {
      this.target = target;
   }
   
   /**
    * Gets the exception message
    * 
    * @return The message
    */
   @Override
   protected String getMessage()
   {
      String location = super.getMessage();
      if (getTarget() != null)
      {
         location += "target: " + getTarget() + ";"; 
      }
      return location;
   }
   
}
