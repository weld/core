package org.jboss.weld.tests;

import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class MyTest
{
   @Test
   @Category(Integration.class)
   public void integration() 
   {
      System.out.println("integration");
   }

   @Test
   @Category(Broken.class)
   public void broken() 
   {
      System.out.println("broken");
   }
   
   @Test
   public void unmarked() 
   {
      System.out.println("unmarked");
   }

   @Test
   @Category({Integration.class, Broken.class})
   public void brokenintegration() 
   {
      System.out.println("brokenintegration");
   }
}
