package org.jboss.weld.tests.xml.broken.clazz;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeansXmlTest
{
   @Deployment @ShouldThrowException(Exception.class)
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
         .alternate(Bar.class)
         .addPackage(BeansXmlTest.class.getPackage());
   }

   @Test
   public void testClassRespectedInBeansXml()
   {
      //assert false; // Arquillian ShouldThrowException marks it as allowed, does not stop @Test from execution
   }
   
}
