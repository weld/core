package org.jboss.webbeans.test.tck;

import org.jboss.webbeans.tck.api.TCKConfiguration;
import org.jboss.webbeans.tck.api.TestSuite;
import org.jboss.webbeans.tck.api.WebBeansTCK;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TCKRunner
{
   
   private WebBeansTCK tck;
   
   @BeforeClass
   public void beforeClass()
   {
      TCKConfiguration configuration = new TCKConfiguration(new BeansImpl(), new ContextsImpl(), new ManagersImpl(), new TestSuite("target/surefire-reports"));
      tck = WebBeansTCK.newInstance(configuration);
   }
  
   @Test
   public void runTCK()
   {
      if (!tck.run())
      {
         throw new AssertionError("TCK run failed, see log");
      }
   }
   
}
