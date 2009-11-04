package org.jboss.weld.tests.resolution.named;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * @author Dan Allen
 */
@Artifact(addCurrentPackage = true)
public class NamedBeanWithBindingTest extends AbstractWeldTest
{
   @Test
   public void testGetNamedBeanWithBinding()
   {
      Bean<?> bean = getCurrentManager().resolve(getCurrentManager().getBeans("namedBeanWithBinding"));
      NamedBeanWithBinding instance = (NamedBeanWithBinding) getCurrentManager().getReference(bean, Object.class, getCurrentManager().createCreationalContext(bean));
      assert instance != null;
   }
}
