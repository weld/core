package org.jboss.weld.test.unit.implementation.proxy;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ProxyTest extends AbstractWeldTest
{
   
   @Test(description="WBRI-122")
   public void testImplementationClassImplementsSerializable()
   {
      Bean<?> bean = getCurrentManager().resolve(getCurrentManager().getBeans("foo"));
      getCurrentManager().getReference(bean, Object.class, getCurrentManager().createCreationalContext(bean));
   }
   
}
