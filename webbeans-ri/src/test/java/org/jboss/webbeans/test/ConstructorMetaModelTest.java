package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.webbeans.Current;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.SimpleConstructor;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.components.Chicken;
import org.jboss.webbeans.test.components.Donkey;
import org.jboss.webbeans.test.components.Duck;
import org.jboss.webbeans.test.components.Goat;
import org.jboss.webbeans.test.components.Goose;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.test.components.Sheep;
import org.jboss.webbeans.test.components.Turkey;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.junit.Before;
import org.junit.Test;

public class ConstructorMetaModelTest
{

   private ContainerImpl container;
   
   private AnnotatedType emptyAnnotatedItem;
   
   @Before
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
      
   }
   
   @Test
   public void testImplicitConstructor()
   {
      SimpleConstructor<Order> constructor = new SimpleComponentModel<Order>(new SimpleAnnotatedType(Order.class), emptyAnnotatedItem, container).getConstructor();
      assert constructor.getConstructor().getDeclaringClass().equals(Order.class);
      assert constructor.getConstructor().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test
   public void testSingleConstructor()
   {
      SimpleConstructor<Donkey> constructor = new SimpleComponentModel<Donkey>(new SimpleAnnotatedType(Donkey.class), emptyAnnotatedItem, container).getConstructor();
      assert constructor.getConstructor().getDeclaringClass().equals(Donkey.class);
      assert constructor.getConstructor().getParameterTypes().length == 1;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getParameters().size() == 1;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(0).getBindingTypes().length == 1;
      assert constructor.getParameters().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @Test
   public void testInitializerAnnotatedConstructor()
   {
      SimpleConstructor<Sheep> constructor = new SimpleComponentModel<Sheep>(new SimpleAnnotatedType(Sheep.class), emptyAnnotatedItem, container).getConstructor();
      assert constructor.getConstructor().getDeclaringClass().equals(Sheep.class);
      assert constructor.getConstructor().getParameterTypes().length == 2;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getConstructor().getParameterTypes()[1].equals(Double.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Double.class);
      assert constructor.getParameters().get(0).getBindingTypes().length == 1;
      assert constructor.getParameters().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
      assert constructor.getParameters().get(1).getBindingTypes().length == 1;
      assert constructor.getParameters().get(1).getBindingTypes()[0].annotationType().equals(Current.class);
   }
   
   @Test
   public void testBindingTypeAnnotatedConstructor()
   {
      SimpleConstructor<Duck> constructor = new SimpleComponentModel<Duck>(new SimpleAnnotatedType(Duck.class), emptyAnnotatedItem, container).getConstructor();
      assert constructor.getConstructor().getDeclaringClass().equals(Duck.class);
      assert constructor.getConstructor().getParameterTypes().length == 2;
      assert constructor.getConstructor().getParameterTypes()[0].equals(String.class);
      assert constructor.getConstructor().getParameterTypes()[1].equals(Integer.class);
      assert constructor.getParameters().size() == 2;
      assert constructor.getParameters().get(0).getType().equals(String.class);
      assert constructor.getParameters().get(1).getType().equals(Integer.class);
      assert constructor.getParameters().get(0).getBindingTypes().length == 1;
      assert constructor.getParameters().get(0).getBindingTypes()[0].annotationType().equals(Current.class);
      assert constructor.getParameters().get(1).getBindingTypes().length == 1;
      assert constructor.getParameters().get(1).getBindingTypes()[0].annotationType().equals(Synchronous.class);
   }
   
   @Test
   public void testTooManyInitializerAnnotatedConstructor()
   {
      boolean exception = false;
      try
      {
         new SimpleComponentModel<Chicken>(new SimpleAnnotatedType(Chicken.class), emptyAnnotatedItem, container);
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
         new SimpleComponentModel<Turkey>(new SimpleAnnotatedType(Turkey.class), emptyAnnotatedItem, container);
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
         new SimpleComponentModel<Goat>(new SimpleAnnotatedType(Goat.class), emptyAnnotatedItem, container);
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
         new SimpleComponentModel<Goose>(new SimpleAnnotatedType(Goose.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
}
