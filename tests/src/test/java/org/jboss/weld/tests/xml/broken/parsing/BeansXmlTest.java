package org.jboss.weld.tests.xml.broken.parsing;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@BeansXml("beans.xml")
//@ExpectedDeploymentException(Exception.class)
public class BeansXmlTest extends AbstractWeldTest
{
   
   @Test
   public void test()
   {
      //assert false;
   }
   
}
