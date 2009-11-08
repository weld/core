package org.jboss.weld.tests.unit.reflection.annotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.Test;

@Synchronous
public class AnnotationTest
{
   
   @Test
   public void testSerializability() throws Throwable
   {
      Synchronous synchronous = AnnotationTest.class.getAnnotation(Synchronous.class);
      deserialize(serialize(synchronous));
   }

   protected byte[] serialize(Object instance) throws IOException
   {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(instance);
      return bytes.toByteArray();
   }

   protected <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException
   {
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return (T) in.readObject();
   }
   
}
