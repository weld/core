package org.jboss.webbeans.test.unit.implementation;

import javassist.util.proxy.ProxyObject;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class EnterpriseBeanProxyTest extends AbstractWebBeansTest
{
   
   /**
    * <a href="https://jira.jboss.org/jira/browse/WBRI-109">WBRI-109</a>
    */
   @Test
   public void testNoInterfaceView() throws Exception
   {
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
