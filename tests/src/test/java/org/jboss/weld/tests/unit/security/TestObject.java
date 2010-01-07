package org.jboss.weld.tests.unit.security;

public class TestObject extends SuperTestObject
{
   private String privateField;
   public String publicField;
   
   private TestObject(String test)
   {
   }
   
   public TestObject() {};
   
   public TestObject(Integer test) {
   }

   private void privateTest(String test)
   {
   }
   
   public String publicTest(String test) 
   {
      return "foo";
   }
}