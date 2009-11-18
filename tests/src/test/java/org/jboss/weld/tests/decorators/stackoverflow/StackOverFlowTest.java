package org.jboss.weld.tests.decorators.stackoverflow;

import java.math.BigDecimal;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@BeansXml("beans.xml")
public class StackOverFlowTest extends AbstractWeldTest
{
   
   @Test(description="WELD-296", groups="broken")
   public void test()
   {
      getCurrentManager().getInstanceByType(PaymentService.class, new AnnotationLiteral<SimpleService>() {}).pay("Pete", new BigDecimal(100));
   }

}
