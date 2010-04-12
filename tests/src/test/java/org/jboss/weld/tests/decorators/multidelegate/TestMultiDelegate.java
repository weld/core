package org.jboss.weld.tests.decorators.multidelegate;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@BeansXml("beans.xml")
public class TestMultiDelegate extends AbstractWeldTest
{
   
   @Test(description="http://seamframework.org/Community/SerializableDecorators")
   public void go() {
   }

}
