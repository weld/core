package org.jboss.webbeans.test.unit.xml.javaeepkg;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.jboss.webbeans.test.unit.xml.javaeepkg.foo.Order;
import org.testng.annotations.Test;

@Artifact
@Classes({Order.class})
@BeansXml("beans.xml")
public class JavaEePkgTest extends AbstractWebBeansTest
{
   @Test
   public void testJavaEePkg()
   {      
      assert getCurrentManager().resolveByType(Order.class).size() == 2;
   }
}