package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.getEmptyAnnotatedType;

import javax.webbeans.Current;

import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.beans.Chicken;
import org.jboss.webbeans.test.beans.Donkey;
import org.jboss.webbeans.test.beans.Duck;
import org.jboss.webbeans.test.beans.Goat;
import org.jboss.webbeans.test.beans.Goose;
import org.jboss.webbeans.test.beans.Order;
import org.jboss.webbeans.test.beans.Sheep;
import org.jboss.webbeans.test.beans.Turkey;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.Test;

@SpecVersion("20080925")
public class ConstructorModelTest extends AbstractTest
{
   
   @Test
   public void testImplicitConstructor()
   {
      SimpleConstructor<Order> constructor = new SimpleBeanModel<Order>(new SimpleAnnotatedType<Order>(Order.class), getEmptyAnnotatedType(Order.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Order.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testSingleConstructor()
   {
      SimpleConstructor<Donkey> constructor = new SimpleBeanModel<Donkey>(new SimpleAnnotatedType<Donkey>(Donkey.class), getEmptyAnnotatedType(Donkey.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Donkey.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 1;
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[0].equals(String.class);
      assert constructor.getParameters().size() == 1;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(0).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(constructor.getParameters().get(0).getBindingTypes(), Current.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testInitializerAnnotatedConstructor()
   {
      SimpleConstructor<Sheep> constructor = new SimpleBeanModel<Sheep>(new SimpleAnnotatedType<Sheep>(Sheep.class), getEmptyAnnotatedType(Sheep.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Sheep.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 2;
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[0].equals(String.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[1].equals(Double.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Double.class);
      assert constructor.getParameters().get(0).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(constructor.getParameters().get(0).getBindingTypes(), Current.class);
      assert constructor.getParameters().get(1).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(constructor.getParameters().get(1).getBindingTypes(), Current.class);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testBindingTypeAnnotatedConstructor()
   {
      SimpleConstructor<Duck> constructor = new SimpleBeanModel<Duck>(new SimpleAnnotatedType<Duck>(Duck.class), getEmptyAnnotatedType(Duck.class), manager).getConstructor();
      assert constructor.getAnnotatedItem().getDelegate().getDeclaringClass().equals(Duck.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes().length == 2;
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[0].equals(String.class);
      assert constructor.getAnnotatedItem().getDelegate().getParameterTypes()[1].equals(Integer.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Integer.class);
      assert constructor.getParameters().get(0).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(constructor.getParameters().get(0).getBindingTypes(), Current.class);
      assert constructor.getParameters().get(1).getBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(constructor.getParameters().get(1).getBindingTypes(), Synchronous.class);
   }
   
   @Test
   public void testTooManyInitializerAnnotatedConstructor()
   {
      boolean exception = false;
      try
      {
         new SimpleBeanModel<Chicken>(new SimpleAnnotatedType<Chicken>(Chicken.class), getEmptyAnnotatedType(Chicken.class), manager);
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
         new SimpleBeanModel<Turkey>(new SimpleAnnotatedType<Turkey>(Turkey.class), getEmptyAnnotatedType(Turkey.class), manager);
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
         new SimpleBeanModel<Goat>(new SimpleAnnotatedType<Goat>(Goat.class), getEmptyAnnotatedType(Goat.class), manager);
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
         new SimpleBeanModel<Goose>(new SimpleAnnotatedType<Goose>(Goose.class), getEmptyAnnotatedType(Goose.class), manager);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testStereotypeOnConstructor()
   {
	   assert false;
   }
   
}
