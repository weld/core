package org.jboss.webbeans.test.unit.implementation.producer.method;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class NamedProducerTest extends AbstractWebBeansTest
{
   @Test
   public void testNamedProducer()
   {
      Bean<?> iemonBean = getCurrentManager().getHighestPrecedenceBean(getCurrentManager().getBeans("iemon"));
      String[] iemon = (String[]) getCurrentManager().getReference(iemonBean, Object.class, getCurrentManager().createCreationalContext(iemonBean));
      assert iemon.length == 3;
      Bean<?> itoenBean = getCurrentManager().getHighestPrecedenceBean(getCurrentManager().getBeans("itoen"));
      String[] itoen = (String[]) getCurrentManager().getReference(itoenBean, Object.class, getCurrentManager().createCreationalContext(itoenBean));
      assert itoen.length == 2;
   }

}
