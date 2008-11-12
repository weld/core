package org.jboss.webbeans.test;

import javax.webbeans.InconsistentSpecializationException;

import org.testng.annotations.Test;

public class SpecializationTest
{
   
   @Test(expectedExceptions=InconsistentSpecializationException.class, groups={"stub", "specialization"})
   public void testInconsistentSpecialization()
   {
      
   }
   
}
