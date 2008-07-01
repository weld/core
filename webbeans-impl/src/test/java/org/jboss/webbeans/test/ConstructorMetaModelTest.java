package org.jboss.webbeans.test;

import javax.webbeans.Current;

import org.jboss.webbeans.ConstructorMetaModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.components.Chicken;
import org.jboss.webbeans.test.components.Donkey;
import org.jboss.webbeans.test.components.Duck;
import org.jboss.webbeans.test.components.Goat;
import org.jboss.webbeans.test.components.Goose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.Sheep;
import org.jboss.webbeans.test.components.Turkey;
import org.junit.Test;

public class ConstructorMetaModelTest
{

   @Test
   public void testImplicitConstructor()
   {
      ConstructorMetaModel<Order> constructor = new ConstructorMetaModel<Order>(Order.class);
      assert constructor.getConstructor().getDeclaringClass().equals(Order.class);
      assert constructor.getConstructor().getParameterTypes().length == 0;
      assert constructor.getInjectedAttributes().size() == 0;
   }
   
   @Test
   public void testSingleConstructor()
   {
      ConstructorMetaModel<Donkey> constructor = new ConstructorMetaModel<Donkey>(Donkey.class);
      assert constructor.getConstructor().getDeclaringClass().equals(Donkey.class);
      assert constructor.getConstructor().getParameterTypes().length == 1;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getInjectedAttributes().size() == 1;
      assert constructor.getInjectedAttributes().get(0).getType().equals(String.class);
      assert constructor.getInjectedAttributes().get(0).getBindingTypes().length == 1;
      assert constructor.getInjectedAttributes().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @Test
   public void testInitializerAnnotatedConstructor()
   {
      ConstructorMetaModel<Sheep> constructor = new ConstructorMetaModel<Sheep>(Sheep.class);
      assert constructor.getConstructor().getDeclaringClass().equals(Sheep.class);
      assert constructor.getConstructor().getParameterTypes().length == 2;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getConstructor().getParameterTypes()[1].equals(Double.class);
      assert constructor.getInjectedAttributes().size() == 2;
      assert constructor.getInjectedAttributes().get(0).getType().equals(String.class);
      assert constructor.getInjectedAttributes().get(1).getType().equals(Double.class);
      assert constructor.getInjectedAttributes().get(0).getBindingTypes().length == 1;
      assert constructor.getInjectedAttributes().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
      assert constructor.getInjectedAttributes().get(1).getBindingTypes().length == 1;
      assert constructor.getInjectedAttributes().get(1).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @Test
   public void testBindingTypeAnnotatedConstructor()
   {
      ConstructorMetaModel<Duck> constructor = new ConstructorMetaModel<Duck>(Duck.class);
      assert constructor.getConstructor().getDeclaringClass().equals(Duck.class);
      assert constructor.getConstructor().getParameterTypes().length == 2;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getConstructor().getParameterTypes()[1].equals(Integer.class);
      assert constructor.getInjectedAttributes().size() == 2;
      assert constructor.getInjectedAttributes().get(0).getType().equals(String.class);
      assert constructor.getInjectedAttributes().get(1).getType().equals(Integer.class);
      assert constructor.getInjectedAttributes().get(0).getBindingTypes().length == 1;
      assert constructor.getInjectedAttributes().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
      assert constructor.getInjectedAttributes().get(1).getBindingTypes().length == 1;
      assert constructor.getInjectedAttributes().get(1).getBindingTypes()[0].annotationType().equals(Synchronous.class);
   }
   
   @Test
   public void testTooManyInitializerAnnotatedConstructor()
   {
      boolean exception = false;
      try
      {
         new ConstructorMetaModel<Chicken>(Chicken.class);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test
   public void testTooManyConstructors()
   {
      boolean exception = false;
      try
      {
         new ConstructorMetaModel<Turkey>(Turkey.class);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test
   public void testTooManyBindingTypeAnnotatedConstructor()
   {
      boolean exception = false;
      try
      {
         new ConstructorMetaModel<Goat>(Goat.class);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test
   public void testBindingTypeAndInitializerAnnotatedConstructor()
   {
      boolean exception = false;
      try
      {
         new ConstructorMetaModel<Goose>(Goose.class);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
}
