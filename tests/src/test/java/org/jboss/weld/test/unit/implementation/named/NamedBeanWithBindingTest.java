package org.jboss.weld.test.unit.implementation.named;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
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
      Bean<?> bean = getCurrentManager().resolve(getCurrentManager().getBeans("namedBeanWithBinding"));
      NamedBeanWithBinding instance = (NamedBeanWithBinding) getCurrentManager().getReference(bean, Object.class, getCurrentManager().createCreationalContext(bean));
      assert instance != null;
   }
}
