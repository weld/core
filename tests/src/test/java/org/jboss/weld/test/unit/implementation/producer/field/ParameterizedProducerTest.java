package org.jboss.weld.test.unit.implementation.producer.field;

import java.util.Collection;
import java.util.List;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedProducerTest extends AbstractWebBeansTest
{

   @Test
   public void testParameterizedListInjection()
   {
      List<String> strings = createContextualInstance(Target.class).getStringList();
      assert strings.size() == 2;

      ParameterizedListInjection item = getCurrentManager().getInstanceByType(ParameterizedListInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testParameterizedCollectionInjection()
   {
      Collection<String> strings = createContextualInstance(Target.class).getStrings();
      assert strings.size() == 2;

      ParameterizedCollectionInjection item = getCurrentManager().getInstanceByType(ParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }
   
   @Test
   public void testIntegerCollectionInjection()
   {
      Collection<Integer> integers = createContextualInstance(Target.class).getIntegers();
      assert integers.size() == 4;

      IntegerCollectionInjection item = getCurrentManager().getInstanceByType(IntegerCollectionInjection.class);
      assert item.getFieldInjection().size() == 4;
      assert item.getValue().size() == 4;
      assert item.getSetterInjection().size() == 4;

   }
   
   @Test
   public void testInstanceList()
   {
        ListInstance listInstance = getCurrentManager().getInstanceByType(ListInstance.class);
        assert listInstance.get().isAmbiguous();
   }
   
   @Test
   public void testTypeParameterInstance()
   {
        ListStringInstance listInstance = getCurrentManager().getInstanceByType(ListStringInstance.class);
        assert listInstance.get().size() == 2;
   }
}
