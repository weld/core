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
 * Helper superclass for exception information
 * 
 * @author Pete Muir
 */
public class Location
{
   // The category of the exception 
   private String type;
   // The bean the exception occurred in
   private String bean;
   // The element the exception occurred in
   private String element;

   /**
    * Constructor
    * 
    * @param type The category
    * @param bean The bean
    * @param element The element
    */
   public Location(String type, String bean, String element)
   {
      super();
      this.type = type;
      this.bean = bean;
      this.element = element;
   }

   /**
    * Gets the type of the exception
    * 
    * @return The type
    */
   public String getType()
   {
      return type;
   }

   /**
    * Sets the type of the exception
    * 
    * @param type The type
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * Gets the bean the exception occurred in
    * 
    * @return The bean
    */
   public String getBean()
   {
      return bean;
   }

   /**
    * Sets the bean the exception occurred in
    * 
    * @param bean The bean
    */
   public void setBean(String bean)
   {
      this.bean = bean;
   }

   /**
    * Gets the element the exception occurred in
    * 
    * @return The element
    */
   public String getElement()
   {
      return element;
   }

   /**
    * Sets the element the exception occurred in
    * 
    * @param element The element
    */
   public void setElement(String element)
   {
      this.element = element;
   }
   
   /**
    * Gets the summarizing message
    * 
    * @return The message
    */
   protected String getMessage()
   {
      String location = "";
      if (getType() != null)
      {
         location += "type: " + getType() + "; ";
      }
      if (getBean() != null)
      {
         location += "bean: " + getBean() + "; ";
      }
      if (getElement() != null)
      {
         location += "element: " + getElement() + "; ";
      }
      return location;
   }
   
   @Override
   public String toString()
   {
      return getMessage();
   }
   
}
