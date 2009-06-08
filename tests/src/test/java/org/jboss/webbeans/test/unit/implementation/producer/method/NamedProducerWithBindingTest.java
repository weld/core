package org.jboss.webbeans.test.unit.implementation.producer.method;

import static org.testng.Assert.assertNotNull;

import java.util.Date;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * @author Dan Allen
 */
@Artifact(addCurrentPackage = true)
public class NamedProducerWithBindingTest extends AbstractWebBeansTest
{
   @Test
   public void testGetNamedProducerWithBinding()
   {
      Date date = (Date) getCurrentManager().getReference(getCurrentManager().getHighestPrecedenceBean(getCurrentManager().getBeans("date")), Object.class);
      assertNotNull(date);
   }
}
