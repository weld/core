/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.security;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.weld.util.reflection.SecureReflections;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionTest
{

   @Test
   public void testGetField() throws NoSuchFieldException
   {
      Assert.assertNotNull(SecureReflections.getField(TestObject.class, "publicField"));
   }

   @Test(expected = NoSuchFieldException.class)
   public void testGetFieldNotFound() throws NoSuchFieldException
   {
      SecureReflections.getField(TestObject.class, "eioota");
   }

   @Test
   public void testGetDeclaredField() throws SecurityException, NoSuchFieldException
   {
      Assert.assertNotNull(SecureReflections.getDeclaredField(TestObject.class, "publicField"));
   }

   @Test(expected = NoSuchFieldException.class)
   public void testGetDeclaredFieldNotFound() throws NoSuchFieldException
   {
      SecureReflections.getDeclaredField(TestObject.class, "eioota");
   }

   @Test
   public void testGetFields()
   {
      Assert.assertEquals(1, SecureReflections.getFields(TestObject.class).length);
   }

   @Test
   public void testGetDeclaredFields()
   {
      Assert.assertEquals(2, SecureReflections.getDeclaredFields(TestObject.class).length);
   }

   @Test
   public void testFieldAccess()
   {
      testAllAccessible(grantAccess(SecureReflections.getDeclaredFields(TestObject.class)));
   }
   
   private AccessibleObject[] grantAccess(AccessibleObject[] objects) {
      for (AccessibleObject object : objects)
      {
         if (object instanceof Field) {
            SecureReflections.ensureAccessible((Field) object);
         } else if (object instanceof Method) {
            SecureReflections.ensureAccessible((Method) object);
         } else if (object instanceof Constructor<?>) {
            SecureReflections.ensureAccessible((Constructor<?>)object);
         }
      }
      return objects;
   }
   
   private void testAllAccessible(AccessibleObject[] objects)
   {
      for (AccessibleObject object : objects)
      {
         if (!object.isAccessible())
         {
            Assert.fail();
         }
      }
   }

   @Test
   public void testGetMethod() throws NoSuchMethodException
   {
      Assert.assertNotNull(SecureReflections.getMethod(TestObject.class, "publicTest", new Class<?>[] { String.class }));
   }

   @Test(expected = NoSuchMethodException.class)
   public void testGetMethodNotFound() throws NoSuchMethodException
   {
      SecureReflections.getMethod(TestObject.class, "xpublicTest", new Class<?>[] { String.class });
   }

   @Test
   public void testGetDeclaredMethod() throws NoSuchMethodException
   {
      Assert.assertNotNull(SecureReflections.getDeclaredMethod(TestObject.class, "publicTest", new Class<?>[] { String.class }));
   }

   @Test(expected = NoSuchMethodException.class)
   public void testGetDeclaredMethodNotFound() throws NoSuchMethodException
   {
      SecureReflections.getDeclaredMethod(TestObject.class, "xpublicTest", new Class<?>[] { String.class });
   }

   @Test
   public void testGetMethods()
   {
      Assert.assertEquals(10, SecureReflections.getMethods(TestObject.class).length);
   }

   @Test
   public void testGetDeclaredMethods()
   {
      Assert.assertEquals(2, SecureReflections.getDeclaredMethods(TestObject.class).length);
   }

   @Test
   public void testMethodAccess()
   {
      testAllAccessible(grantAccess(SecureReflections.getDeclaredMethods(TestObject.class)));
   }

   @Test
   public void testGetConstructor() throws NoSuchMethodException
   {
      Assert.assertNotNull(SecureReflections.getConstructor(TestObject.class, new Class<?>[] { Integer.class }));
   }

   @Test(expected = NoSuchMethodException.class)
   public void testGetConstructorNotFound() throws NoSuchMethodException
   {
      SecureReflections.getConstructor(TestObject.class, new Class<?>[] { Float.class });
   }

   @Test
   public void testGetDeclaredConstructor() throws NoSuchMethodException
   {
      Assert.assertNotNull(SecureReflections.getDeclaredConstructor(TestObject.class, new Class<?>[] { String.class }));
   }

   @Test(expected = NoSuchMethodException.class)
   public void testGetDeclaredConstructorNotFound() throws NoSuchMethodException
   {
      SecureReflections.getDeclaredConstructor(TestObject.class, new Class<?>[] { Float.class });
   }

   @Test
   public void testGetConstructors()
   {
      Assert.assertEquals(2, SecureReflections.getConstructors(TestObject.class).length);
   }

   @Test
   public void testGetDeclaredConstructors()
   {
      Assert.assertEquals(3, SecureReflections.getDeclaredConstructors(TestObject.class).length);
   }

   @Test
   public void testConstructorAccess()
   {
      testAllAccessible(grantAccess(SecureReflections.getDeclaredConstructors(TestObject.class)));
   }

   @Test
   public void testNewInstance() throws InstantiationException, IllegalAccessException
   {
      Assert.assertNotNull(SecureReflections.newInstance(TestObject.class));
   }

   @Test
   public void testInvoke() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      TestObject to = new TestObject();
      Method m = TestObject.class.getMethod("publicTest", new Class<?>[] { String.class });
      Assert.assertEquals("foo", SecureReflections.invoke(to, m, ""));
   }
   
   @Test
   public void testLookupMethod() throws NoSuchMethodException 
   {
      Assert.assertNotNull(SecureReflections.lookupMethod(TestObject.class, "rootOfAllEvil", new Class<?>[]{}));
   }
   
   @Test(expected = NoSuchMethodException.class)
   public void testLookupMethodNotFound() throws NoSuchMethodException 
   {
      Assert.assertNotNull(SecureReflections.lookupMethod(TestObject.class, "eioota", new Class<?>[]{}));
   }
}
