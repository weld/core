package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.TypeLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedProducerTest extends AbstractWebBeansTest
{

   @Test
   public void testParameterizedListInjection()
   {
      assert getCurrentManager().getInstanceByType(new TypeLiteral<List<String>>()
      {
      }).size() == 2;

      ParameterizedListInjection item = getCurrentManager().getInstanceByType(ParameterizedListInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testParameterizedCollectionInjection()
   {
      assert getCurrentManager().getInstanceByType(new TypeLiteral<Collection<String>>()
      {
      }).size() == 2;

      ParameterizedCollectionInjection item = getCurrentManager().getInstanceByType(ParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testNoParameterizedCollectionInjection()
   {
      assert getCurrentManager().getInstanceByType(Collection.class).size() == 3;

      NoParameterizedCollectionInjection item = getCurrentManager().getInstanceByType(NoParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 3;
      assert item.getValue().size() == 3;
      assert item.getSetterInjection().size() == 3;

   }
   
   @Test
   public void testIntegerCollectionInjection()
   {
      assert getCurrentManager().getInstanceByType(new TypeLiteral<Collection<Integer>>(){}).size() == 4;

      IntegerCollectionInjection item = getCurrentManager().getInstanceByType(IntegerCollectionInjection.class);
      assert item.getFieldInjection().size() == 4;
      assert item.getValue().size() == 4;
      assert item.getSetterInjection().size() == 4;

   }
   
   @Test
   public void testInstanceList()
   {
        ListInstance listInstance = getCurrentManager().getInstanceByType(ListInstance.class);
        assert listInstance.get().size() == 3;
   }
   
   @Test
   public void testTypeParameterInstance()
   {
        ListStringInstance listInstance = getCurrentManager().getInstanceByType(ListStringInstance.class);
        assert listInstance.get().size() == 2;
   }
}
