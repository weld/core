package org.jboss.webbeans.test.unit.implementation.named;

import static org.testng.Assert.assertNotNull;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * @author Dan Allen
 */
@Artifact(addCurrentPackage = true)
public class NamedBeanWithBindingTest extends AbstractWebBeansTest
{
   @Test
   public void testGetNamedBeanWithBinding()
   {
      NamedBeanWithBinding bean = (NamedBeanWithBinding) getCurrentManager().getInstanceByName("namedBeanWithBinding");
      assertNotNull(bean);
   }
}
