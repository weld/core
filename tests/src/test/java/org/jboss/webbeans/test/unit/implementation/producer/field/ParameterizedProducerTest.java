package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.List;

import javax.inject.TypeLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedProducerTest extends AbstractWebBeansTest
{

   @Test
   public void testInjectManagerProducer()
   {
      assert manager.getInstanceByType(new TypeLiteral<List<String>>(){}).size() == 2;

      ParameterizedListInjection item = manager.getInstanceByType(ParameterizedListInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }
}
