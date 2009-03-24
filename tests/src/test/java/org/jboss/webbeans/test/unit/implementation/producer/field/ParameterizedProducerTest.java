package org.jboss.webbeans.test.unit.implementation.producer.field;

import java.util.Collection;
import java.util.List;

import javax.inject.TypeLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ParameterizedProducerTest extends AbstractWebBeansTest
{

   @Test
   public void testParameterizedListInjection()
   {
      assert manager.getInstanceByType(new TypeLiteral<List<String>>()
      {
      }).size() == 2;

      ParameterizedListInjection item = manager.getInstanceByType(ParameterizedListInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testParameterizedCollectionInjection()
   {
      assert manager.getInstanceByType(new TypeLiteral<Collection<String>>()
      {
      }).size() == 2;

      ParameterizedCollectionInjection item = manager.getInstanceByType(ParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 2;
      assert item.getValue().size() == 2;
      assert item.getSetterInjection().size() == 2;

   }

   @Test
   public void testNoParameterizedCollectionInjection()
   {
      assert manager.getInstanceByType(Collection.class).size() == 3;

      NoParameterizedCollectionInjection item = manager.getInstanceByType(NoParameterizedCollectionInjection.class);
      assert item.getFieldInjection().size() == 3;
      assert item.getValue().size() == 3;
      assert item.getSetterInjection().size() == 3;

   }
   
   @Test
   public void testIntegerCollectionInjection()
   {
      assert manager.getInstanceByType(new TypeLiteral<Collection<Integer>>(){}).size() == 4;

      IntegerCollectionInjection item = manager.getInstanceByType(IntegerCollectionInjection.class);
      assert item.getFieldInjection().size() == 4;
      assert item.getValue().size() == 4;
      assert item.getSetterInjection().size() == 4;

   }
}
