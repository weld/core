package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.webbeans.Current;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.RemoteComponentModel;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Baboon;
import org.jboss.webbeans.test.components.TameApe;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoteComponentModelTest
{
   
   private ContainerImpl container;
   private AnnotatedType<?> emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
   }
   
   @Test @SpecAssertion(section="3.5")
   public void testApiTypes() throws SecurityException, NoSuchMethodException
   {
      RemoteComponentModel<Baboon> baboonModel = new RemoteComponentModel<Baboon>(new SimpleAnnotatedType<Baboon>(Baboon.class), emptyAnnotatedItem, container);
      assert baboonModel.getApiTypes().contains(Baboon.class);
      assert baboonModel.getApiTypes().contains(Animal.class);
   }
   
   @Test @SpecAssertion(section="3.5.4")
   public void testDefaultName() throws SecurityException, NoSuchMethodException
   {
      RemoteComponentModel<TameApe> tameApeModel = new RemoteComponentModel<TameApe>(new SimpleAnnotatedType<TameApe>(TameApe.class), emptyAnnotatedItem, container);
      assert tameApeModel.getName().equals("tameApe");
   }
   
   @Test @SpecAssertion(section="3.5.1")
   public void testDefaultBindingType() throws SecurityException, NoSuchMethodException
   {
      RemoteComponentModel<Baboon> baboonModel = new RemoteComponentModel<Baboon>(new SimpleAnnotatedType<Baboon>(Baboon.class), emptyAnnotatedItem, container);
      assert baboonModel.getBindingTypes().size() == 1;
      assert baboonModel.getBindingTypes().iterator().next().annotationType().equals(Current.class);
   }
   
   @Test
   public void testBindingType() throws SecurityException, NoSuchMethodException
   {
      RemoteComponentModel<TameApe> tameApeModel = new RemoteComponentModel<TameApe>(new SimpleAnnotatedType<TameApe>(TameApe.class), emptyAnnotatedItem, container);
      assert tameApeModel.getBindingTypes().size() == 1;
      assert tameApeModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
}
