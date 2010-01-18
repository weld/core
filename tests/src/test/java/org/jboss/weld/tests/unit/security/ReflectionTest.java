package org.jboss.weld.tests.unit.security;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.weld.util.reflection.SecureReflections;
import org.testng.annotations.Test;

public class ReflectionTest
{

   @Test
   public void testGetField() throws NoSuchFieldException
   {
      assert SecureReflections.getField(TestObject.class, "publicField") != null;
   }

   @Test(expectedExceptions = NoSuchFieldException.class)
   public void testGetFieldNotFound() throws NoSuchFieldException
   {
      SecureReflections.getField(TestObject.class, "eioota");
   }

   @Test
   public void testGetDeclaredField() throws SecurityException, NoSuchFieldException
   {
      assert SecureReflections.getDeclaredField(TestObject.class, "publicField") != null;
   }

   @Test(expectedExceptions = NoSuchFieldException.class)
   public void testGetDeclaredFieldNotFound() throws NoSuchFieldException
   {
      SecureReflections.getDeclaredField(TestObject.class, "eioota");
   }

   @Test
   public void testGetFields()
   {
      assert SecureReflections.getFields(TestObject.class).length == 1;
   }

   @Test
   public void testGetDeclaredFields()
   {
      assert SecureReflections.getDeclaredFields(TestObject.class).length == 2;
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
            assert false;
         }
      }
   }

   @Test
   public void testGetMethod() throws NoSuchMethodException
   {
      assert SecureReflections.getMethod(TestObject.class, "publicTest", new Class<?>[] { String.class }) != null;
   }

   @Test(expectedExceptions = NoSuchMethodException.class)
   public void testGetMethodNotFound() throws NoSuchMethodException
   {
      SecureReflections.getMethod(TestObject.class, "xpublicTest", new Class<?>[] { String.class });
   }

   @Test
   public void testGetDeclaredMethod() throws NoSuchMethodException
   {
      assert SecureReflections.getDeclaredMethod(TestObject.class, "publicTest", new Class<?>[] { String.class }) != null;
   }

   @Test(expectedExceptions = NoSuchMethodException.class)
   public void testGetDeclaredMethodNotFound() throws NoSuchMethodException
   {
      SecureReflections.getDeclaredMethod(TestObject.class, "xpublicTest", new Class<?>[] { String.class });
   }

   @Test
   public void testGetMethods()
   {
      assert SecureReflections.getMethods(TestObject.class).length == 10;
   }

   @Test
   public void testGetDeclaredMethods()
   {
      assert SecureReflections.getDeclaredMethods(TestObject.class).length == 2;
   }

   @Test
   public void testMethodAccess()
   {
      testAllAccessible(grantAccess(SecureReflections.getDeclaredMethods(TestObject.class)));
   }

   @Test
   public void testGetConstructor() throws NoSuchMethodException
   {
      assert SecureReflections.getConstructor(TestObject.class, new Class<?>[] { Integer.class }) != null;
   }

   @Test(expectedExceptions = NoSuchMethodException.class)
   public void testGetConstructorNotFound() throws NoSuchMethodException
   {
      SecureReflections.getConstructor(TestObject.class, new Class<?>[] { Float.class });
   }

   @Test
   public void testGetDeclaredConstructor() throws NoSuchMethodException
   {
      assert SecureReflections.getDeclaredConstructor(TestObject.class, new Class<?>[] { String.class }) != null;
   }

   @Test(expectedExceptions = NoSuchMethodException.class)
   public void testGetDeclaredConstructorNotFound() throws NoSuchMethodException
   {
      SecureReflections.getDeclaredConstructor(TestObject.class, new Class<?>[] { Float.class });
   }

   @Test
   public void testGetConstructors()
   {
      assert SecureReflections.getConstructors(TestObject.class).length == 2;
   }

   @Test
   public void testGetDeclaredConstructors()
   {
      assert SecureReflections.getDeclaredConstructors(TestObject.class).length == 3;
   }

   @Test
   public void testConstructorAccess()
   {
      testAllAccessible(grantAccess(SecureReflections.getDeclaredConstructors(TestObject.class)));
   }

   @Test
   public void testNewInstance() throws InstantiationException, IllegalAccessException
   {
      assert SecureReflections.newInstance(TestObject.class) != null;
   }

   @Test
   public void testInvoke() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      TestObject to = new TestObject();
      Method m = TestObject.class.getMethod("publicTest", new Class<?>[] { String.class });
      assert SecureReflections.invoke(to, m, "").equals("foo");
   }
   
   @Test
   public void testLookupMethod() throws NoSuchMethodException 
   {
      assert SecureReflections.lookupMethod(TestObject.class, "rootOfAllEvil", new Class<?>[]{}) != null;
   }
   
   @Test(expectedExceptions = NoSuchMethodException.class)
   public void testLookupMethodNotFound() throws NoSuchMethodException 
   {
      assert SecureReflections.lookupMethod(TestObject.class, "eioota", new Class<?>[]{}) != null;
   }
   

}
