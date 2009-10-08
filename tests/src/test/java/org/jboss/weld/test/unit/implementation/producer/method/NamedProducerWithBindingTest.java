package org.jboss.weld.test.unit.implementation.producer.method;

import static org.testng.Assert.assertNotNull;

import java.util.Date;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
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
      Bean<?> bean = getCurrentManager().resolve(getCurrentManager().getBeans("date"));
      Date date = (Date) getCurrentManager().getReference(bean, Object.class, getCurrentManager().createCreationalContext(bean));
      assertNotNull(date);
   }
}
