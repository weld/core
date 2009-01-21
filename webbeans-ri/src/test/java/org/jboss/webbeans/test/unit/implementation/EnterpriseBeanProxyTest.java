package org.jboss.webbeans.test.unit.implementation;

import javassist.util.proxy.ProxyObject;

import org.jboss.webbeans.tck.AbstractTest;
import org.testng.annotations.Test;

public class EnterpriseBeanProxyTest extends AbstractTest
{
   
   /**
    * <a href="https://jira.jboss.org/jira/browse/WBRI-109">WBRI-109</a>
    */
   @Test
   public void testNoInterfaceView() throws Exception
   {
      deployBeans(Mouse.class);
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            Object mouse = manager.getInstanceByType(Mouse.class);
            assert mouse instanceof ProxyObject;
            assert mouse instanceof Mouse;
         }
         
      }.run();
      
   }
   
}
