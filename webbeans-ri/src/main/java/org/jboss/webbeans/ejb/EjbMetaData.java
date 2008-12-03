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

package org.jboss.webbeans.ejb;

import static org.jboss.webbeans.ejb.EJB.MESSAGE_DRIVEN_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.REMOVE_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.SINGLETON_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.STATEFUL_ANNOTATION;
import static org.jboss.webbeans.ejb.EJB.STATELESS_ANNOTATION;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.MESSAGE_DRIVEN;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.SINGLETON;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.STATEFUL;
import static org.jboss.webbeans.ejb.EjbMetaData.EjbType.STATELESS;

import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.util.Strings;

/**
 * EJB metadata
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class EjbMetaData<T>
{
   // The known EJB types
   public enum EjbType
   {
      STATELESS, STATEFUL, SINGLETON, MESSAGE_DRIVEN;
   }

   // The type of the EJB
   private EjbType ejbType;
   // The remove methods
   private List<AnnotatedMethod<Object>> removeMethods = new ArrayList<AnnotatedMethod<Object>>();
   // The destructor methods
   private List<AnnotatedMethod<Object>> destructorMethods = new ArrayList<AnnotatedMethod<Object>>();
   // The remove methods with no arguments
   private List<AnnotatedMethod<Object>> noArgsRemoveMethods = new ArrayList<AnnotatedMethod<Object>>();

   // TODO Populate this from web.xml
   // The EJB link jndi name
   private String ejbLinkJndiName;

   // TODO Initialize this based on the EJB 3.1 spec
   // The default JNDI name
   private String defaultJndiName;

   // TODO Initialize the ejb name
   // The EJB name
   private String ejbName;

   // The abstracted type
   private AnnotatedClass<T> type;

   /**
    * Constrcutor
    * 
    * Creates a new abstracted class and delegates to another constructor
    * 
    * @param type The type
    */
   public EjbMetaData(Class<T> type)
   {
      this(new AnnotatedClassImpl<T>(type));
   }

   /**
    * Constructor
    * 
    * Initializes the class based on information from the abstracted class.
    * Detects the EJB type and remove/destructor methods
    * 
    * @param type The abstracted class
    */
   public EjbMetaData(AnnotatedClass<T> type)
   {
      // TODO Merge in ejb-jar.xml
      this.type = type;
      if (type.isAnnotationPresent(STATELESS_ANNOTATION))
      {
         this.ejbType = STATELESS;
         // TODO Has to be done here? If they are not parsed, they can't be
         // detected later on (EnterpriseBean remove method init)
         if (type.getAnnotatedMethods(Destructor.class).size() > 0)
         {
            throw new DefinitionException("Stateless enterprise beans cannot have @Destructor methods");
         }
      }
      else if (type.isAnnotationPresent(STATEFUL_ANNOTATION))
      {
         this.ejbType = STATEFUL;
         for (AnnotatedMethod<Object> removeMethod : type.getAnnotatedMethods(REMOVE_ANNOTATION))
         {
            removeMethods.add(removeMethod);
            if (removeMethod.getParameters().size() == 0)
            {
               noArgsRemoveMethods.add(removeMethod);
            }
         }
         for (AnnotatedMethod<Object> destructorMethod : type.getAnnotatedMethods(Destructor.class))
         {
            destructorMethods.add(destructorMethod);
         }
      }
      else if (type.isAnnotationPresent(MESSAGE_DRIVEN_ANNOTATION))
      {
         this.ejbType = MESSAGE_DRIVEN;
      }
      else if (type.isAnnotationPresent(SINGLETON_ANNOTATION))
      {
         this.ejbType = SINGLETON;
      }
   }

   /**
    * Indicates if the EJB is a stateless one
    * 
    * @return True if stateless, false otherwise
    */
   public boolean isStateless()
   {
      return STATELESS.equals(ejbType);
   }

   /**
    * Indicates if the EJB is a stateful one
    * 
    * @return True if stateful, false otherwise
    */
   public boolean isStateful()
   {
      return STATEFUL.equals(ejbType);
   }

   /**
    * Indicates if the EJB is a MDB
    * 
    * @return True if MDB, false otherwise
    */
   public boolean isMessageDriven()
   {
      return MESSAGE_DRIVEN.equals(ejbType);
   }

   /**
    * Indicates if the EJB is a singleton
    * 
    * @return True if singleton, false otherwise
    */
   public boolean isSingleton()
   {
      return SINGLETON.equals(ejbType);
   }

   /**
    * Indicates if class really is of a know EJB type
    * 
    * @return True if EJB, false otherwise
    */

   public boolean isEjb()
   {
      return ejbType != null;
   }

   /**
    * Gets the EJB link JNDI name
    * 
    * @return The name
    */
   public String getEjbLinkJndiName()
   {
      return ejbLinkJndiName;
   }

   /**
    * Gets the default JNDI name
    * 
    * @return The name
    */
   public String getDefaultJndiName()
   {
      return defaultJndiName;
   }

   /**
    * Gets the EJB name
    * 
    * @return The name
    */
   public String getEjbName()
   {
      return ejbName;
   }

   public Class<T> getType()
   {
      return type.getType();
   }

   /**
    * Gets the list of remove method abstractions
    * 
    * @return The list of remove methods. An empty list is returned if there are
    *         none.
    */
   public List<AnnotatedMethod<Object>> getRemoveMethods()
   {
      return removeMethods;
   }

   /**
    * Gets a list of destructor method abstractions
    * 
    * @return The list of destructor methods. An empty list is returned if there
    *         are none.
    */
   public List<AnnotatedMethod<Object>> getDestructorMethods()
   {
      return destructorMethods;
   }

   /**
    * Gets the list of remove method abstractions that take no arguments
    * 
    * @return The list of remove methods without arguments. An empty list is
    *         returned if there are none.
    */
   public List<AnnotatedMethod<Object>> getNoArgsRemoveMethods()
   {
      return noArgsRemoveMethods;
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("EJB metadata model\n");
      buffer.append("EJB name: " + ejbName + "\n");
      buffer.append("EJB type: " + ejbType + "\n");
      buffer.append("EJB link JNDI name " + ejbLinkJndiName + "\n");
      buffer.append("Default JNDI name: " + defaultJndiName + "\n");
      buffer.append("Type: " + type.toString() + "\n");
      buffer.append(Strings.collectionToString("Desctructor methods: ", getDestructorMethods()));
      buffer.append(Strings.collectionToString("Remove methods: ", getRemoveMethods()));
      buffer.append(Strings.collectionToString("No-args remove methods: ", getNoArgsRemoveMethods()));
      return buffer.toString();
   }

}
