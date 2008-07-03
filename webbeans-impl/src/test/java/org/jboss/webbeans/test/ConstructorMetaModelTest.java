package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.webbeans.Current;

import org.jboss.webbeans.ComponentMetaModel;
import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.injectable.ConstructorMetaModel;
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
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Before;
import org.junit.Test;

public class ConstructorMetaModelTest
{

   private ContainerImpl container;
   
   private AnnotatedItem emptyAnnotatedItem;
   
   @Before
   public void before()
   {
      emptyAnnotatedItem = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
      
   }
   
   @Test
   public void testImplicitConstructor()
   {
      ConstructorMetaModel<Order> constructor = new ComponentMetaModel<Order>(new ClassAnnotatedItem(Order.class), emptyAnnotatedItem, container).getConstructor();
      assert constructor.getConstructor().getDeclaringClass().equals(Order.class);
      assert constructor.getConstructor().getParameterTypes().length == 0;
      assert constructor.getParameters().size() == 0;
   }
   
   @Test
   public void testSingleConstructor()
   {
      ConstructorMetaModel<Donkey> constructor = new ComponentMetaModel<Donkey>(new ClassAnnotatedItem(Donkey.class), emptyAnnotatedItem, container).getConstructor();
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
      ConstructorMetaModel<Sheep> constructor = new ComponentMetaModel<Sheep>(new ClassAnnotatedItem(Sheep.class), emptyAnnotatedItem, container).getConstructor();
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
      ConstructorMetaModel<Duck> constructor = new ComponentMetaModel<Duck>(new ClassAnnotatedItem(Duck.class), emptyAnnotatedItem, container).getConstructor();
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
         new ComponentMetaModel<Chicken>(new ClassAnnotatedItem(Chicken.class), emptyAnnotatedItem, container);
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
         new ComponentMetaModel<Turkey>(new ClassAnnotatedItem(Turkey.class), emptyAnnotatedItem, container);
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
         new ComponentMetaModel<Goat>(new ClassAnnotatedItem(Goat.class), emptyAnnotatedItem, container);
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
         new ComponentMetaModel<Goose>(new ClassAnnotatedItem(Goose.class), emptyAnnotatedItem, container);
      }
      catch (Exception e) 
      {
         exception = true;
      }
      assert exception;
      
   }
   
}
