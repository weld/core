package org.jboss.weld.tests.producer.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class NamedProducerTest extends AbstractWeldTest
{
   
   @Test
   public void testNamedProducer()
   {
      Bean<?> iemonBean = getCurrentManager().resolve(getCurrentManager().getBeans("iemon"));
      String[] iemon = (String[]) getCurrentManager().getReference(iemonBean, Object.class, getCurrentManager().createCreationalContext(iemonBean));
      assert iemon.length == 3;
      Bean<?> itoenBean = getCurrentManager().resolve(getCurrentManager().getBeans("itoen"));
      String[] itoen = (String[]) getCurrentManager().getReference(itoenBean, Object.class, getCurrentManager().createCreationalContext(itoenBean));
      assert itoen.length == 2;
   }
   
   @Test
   public void testDefaultNamedProducerMethod() 
   {
      Set<Bean<?>> beans = getCurrentManager().getBeans(JmsTemplate.class);
      assert beans.size() == 2;
      List<String> beanNames = new ArrayList<String>(Arrays.asList("errorQueueTemplate", "logQueueTemplate"));
      for (Bean<?> b : beans)
      {
         beanNames.remove(b.getName());
      }
      assert beanNames.isEmpty();
   }

}
