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
package org.jboss.weld.util.reflection;

import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;

/**
 * Helper class for doing work in a privileged context under the
 * "weld.reflection" permission
 */
abstract class SecureReflectionAccess
{
   /**
    * Performs the work and returns the result
    * 
    * @return The value of the operation
    * @throws Exception If the operation failed
    */
   public Object run() throws Exception
   {
//      SecurityManager securityManager = System.getSecurityManager();
//      if (securityManager != null)
//      {
//         if (true)
//         {
//            throw new SecurityException("Privileged execution disabled for now to prevent possible leakage, uncomment to play around with it --Nik");
//         }
//         securityManager.checkPermission(new RuntimePermission("weld.reflection"));
//         return AccessController.doPrivileged(new PrivilegedExceptionAction()
//         {
//            public Object run() throws Exception
//            {
//               return work();
//            }
//         });
//      }
//      else
//      {
         return work();
//      }
   }

   /**
    * Runs the work and wraps the exception in a RuntimeException
    * 
    * @return The result of the work
    */
   public Object runAndWrap()
   {
      try
      {
         return run();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Runs the work and unwraps and NoSuchFieldException from a possible
    * PrivilegedActionException. Wraps any other exceptions in RuntimeException
    * 
    * @return The result of the work (usually a Field)
    * @throws NoSuchFieldException If a field with the specified name is not
    *            found.
    */
   public Object runAsFieldAccess() throws NoSuchFieldException
   {
      try
      {
         return run();
      }
      catch (PrivilegedActionException e)
      {
         if (e.getCause() instanceof NoSuchFieldException)
         {
            throw (NoSuchFieldException) e.getCause();
         }
         throw new RuntimeException(e);
      }
      catch (NoSuchFieldException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Runs the work and unwraps and NoSuchMethodException from a possible
    * PrivilegedActionException. Wraps any other exceptions in RuntimeException
    * 
    * @return The result of the work (usually a Method)
    * @throws NoSuchMethodException If a method with the specified name is not
    *            found.
    */
   public Object runAsMethodAccess() throws NoSuchMethodException
   {
      try
      {
         return run();
      }
      catch (PrivilegedActionException e)
      {
         if (e.getCause() instanceof NoSuchMethodException)
         {
            throw (NoSuchMethodException) e.getCause();
         }
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Runs the work and unwraps any IllegalAccessException,
    * IllegalArgumentException or InvocationTargetException from a possible
    * PrivilegedActionException. Wraps any other exceptions in RuntimeException
    * 
    * @return The return value of the method invoked
    * @throws IllegalAccessException If this Method object enforces Java
    *            language access control and the underlying method is
    *            inaccessible.
    * @throws IllegalArgumentException If the method is an instance method and
    *            the specified object argument is not an instance of the class
    *            or interface declaring the underlying method (or of a subclass
    *            or implementor thereof); if the number of actual and formal
    *            parameters differ; if an unwrapping conversion for primitive
    *            arguments fails; or if, after possible unwrapping, a parameter
    *            value cannot be converted to the corresponding formal parameter
    *            type by a method invocation conversion.
    * @throws InvocationTargetException I the underlying method throws an
    *            exception.
    */
   public Object runAsInvocation() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      try
      {
         return run();
      }
      catch (PrivilegedActionException e)
      {
         if (e.getCause() instanceof IllegalAccessException)
         {
            throw (IllegalAccessException) e.getCause();
         }
         else if (e.getCause() instanceof IllegalArgumentException)
         {
            throw (IllegalArgumentException) e.getCause();
         }
         else if (e.getCause() instanceof InvocationTargetException)
         {
            throw (InvocationTargetException) e.getCause();
         }
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw (IllegalAccessException) e;
      }
      catch (IllegalArgumentException e)
      {
         throw (IllegalArgumentException) e;
      }
      catch (InvocationTargetException e)
      {
         throw (InvocationTargetException) e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Runs the work and unwraps any IllegalAccessException,
    * InstantiationException or IllegalAccessException from a possible
    * PrivilegedActionException. Wraps any other exceptions in RuntimeException
    * 
    * @return The result of the work (usually a new instance)
    * @throws InstantiationException If the class or its nullary constructor is
    *            not accessible.
    * @throws IllegalAccessException If this Class represents an abstract class,
    *            an interface, an array class, a primitive type, or void; or if
    *            the class has no nullary constructor; or if the instantiation
    *            fails for some other reason.
    */
   public Object runAsInstantiation() throws InstantiationException, IllegalAccessException
   {
      try
      {
         return run();
      }
      catch (PrivilegedActionException e)
      {
         if (e.getCause() instanceof InstantiationException)
         {
            throw (InstantiationException) e.getCause();
         }
         else if (e.getCause() instanceof IllegalAccessException)
         {
            throw (IllegalAccessException) e.getCause();
         }
         throw new RuntimeException(e);
      }
      catch (InstantiationException e)
      {
         throw (InstantiationException) e;
      }
      catch (IllegalAccessException e)
      {
         throw (IllegalAccessException) e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected abstract Object work() throws Exception;

}
