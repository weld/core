package org.jboss.weld.tests.producer.field;

import java.util.Collection;
import java.util.List;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedProducerTest extends AbstractWeldTest
{

   @Test
   public void testParameterizedListInjection()
   {
      List<String> strings = getReference(Target.class).getStringList();
      assert strings.size() == 2;

      ParameterizedListInjection item = getReference(ParameterizedListInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testParameterizedCollectionInjection()
   {
      Collection<String> strings = getReference(Target.class).getStrings();
      assert strings.size() == 2;

      ParameterizedCollectionInjection item = getReference(ParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }
   
   @Test
   public void testIntegerCollectionInjection()
   {
      Collection<Integer> integers = getReference(Target.class).getIntegers();
      assert integers.size() == 4;

      IntegerCollectionInjection item = getReference(IntegerCollectionInjection.class);
      assert item.getFieldInjection().size() == 4;
      assert item.getValue().size() == 4;
      assert item.getSetterInjection().size() == 4;

   }
   
   @Test
   public void testInstanceList()
   {
        ListInstance listInstance = getReference(ListInstance.class);
        assert listInstance.get().isAmbiguous();
   }
   
   @Test
   public void testTypeParameterInstance()
   {
        ListStringInstance listInstance = getReference(ListStringInstance.class);
        assert listInstance.get().size() == 2;
   }
}
