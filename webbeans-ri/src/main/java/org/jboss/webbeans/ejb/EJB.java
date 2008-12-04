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

import java.lang.annotation.Annotation;

import org.jboss.webbeans.util.Reflections;

/**
 * Utility class for EJB annotations etc
 * 
 * @author Pete Muir
 */
public class EJB
{

   public @interface Dummy
   {
   }

   // Annotation instances
   public static final Class<? extends Annotation> STATELESS_ANNOTATION;
   public static final Class<? extends Annotation> STATEFUL_ANNOTATION;
   public static final Class<? extends Annotation> MESSAGE_DRIVEN_ANNOTATION;
   public static final Class<? extends Annotation> FAKE_MESSAGE_DRIVEN_ANNOTATION;
   public static final Class<? extends Annotation> SINGLETON_ANNOTATION;
   public static final Class<? extends Annotation> REMOVE_ANNOTATION;

   /**
    * Static initialization block
    */
   static
   {
      STATELESS_ANNOTATION = classForName("javax.ejb.Stateless");
      STATEFUL_ANNOTATION = classForName("javax.ejb.Stateful");
      MESSAGE_DRIVEN_ANNOTATION = classForName("javax.ejb.MessageDriven");
      // Fake MDB for tests
      FAKE_MESSAGE_DRIVEN_ANNOTATION = classForName("org.jboss.webbeans.test.annotations.MessageDriven");
      // FIXME Faking singleton for tests
      SINGLETON_ANNOTATION = classForName("org.jboss.webbeans.test.annotations.Singleton");
      // SINGLETON_ANNOTATION = classForName("javax.ejb.Singleton");
      REMOVE_ANNOTATION = classForName("javax.ejb.Remove");
   }

   /**
    * Initializes an annotation class
    * 
    * @param name The name of the annotation class
    * @return The instance of the annotation. Returns a dummy if the class was
    *         not found
    */
   @SuppressWarnings("unchecked")
   private static Class<? extends Annotation> classForName(String name)
   {
      try
      {
         return (Class<? extends Annotation>) Reflections.classForName(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         return Dummy.class;
      }
   }

}
