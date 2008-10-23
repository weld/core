package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.webbeans.Current;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.RemoteComponentModel;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.components.Animal;
import org.jboss.webbeans.test.components.Baboon;
import org.jboss.webbeans.test.components.Orangutan;
import org.jboss.webbeans.test.components.TameOrangutan;
import org.jboss.webbeans.test.components.broken.Chimpanzee;
import org.jboss.webbeans.test.components.broken.Gibbon;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoteComponentModelTest
{
   
   /*private ManagerImpl container;
   private AnnotatedType<?> emptyAnnotatedItem;
   
   @BeforeMethod
   public void before()
   {
      emptyAnnotatedItem = new SimpleAnnotatedType(null, new HashMap<Class<? extends Annotation>, Annotation>());
      container = new MockContainerImpl(null);
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testSingleStereotype()
   {
	   assert false;
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
      RemoteComponentModel<TameOrangutan> tameApeModel = new RemoteComponentModel<TameOrangutan>(new SimpleAnnotatedType<TameOrangutan>(TameOrangutan.class), emptyAnnotatedItem, container);
      assert tameApeModel.getName().equals("tameOrangutan");
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
      RemoteComponentModel<TameOrangutan> tameOrangutanModel = new RemoteComponentModel<TameOrangutan>(new SimpleAnnotatedType<TameOrangutan>(TameOrangutan.class), emptyAnnotatedItem, container);
      assert tameOrangutanModel.getBindingTypes().size() == 1;
      assert tameOrangutanModel.getBindingTypes().iterator().next().annotationType().equals(Tame.class);
   }
   
   @Test(groups="remoteComponentInXml") @SpecAssertion(section="3.5.2")
   public void testRemoteComponentDeclaredInXml()
   {
      assert false;
   }
   
   @Test @SpecAssertion(section="3.5.3")
   public void testSFSBMustHaveRemoveMethod()
   {
      // TODO How do we check this?
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="3.5.3")
   public void testRemoveMethodCannotHaveParameters()
   {
      RemoteComponentModel<Chimpanzee> chimpanzeeModel = new RemoteComponentModel<Chimpanzee>(new SimpleAnnotatedType<Chimpanzee>(Chimpanzee.class), emptyAnnotatedItem, container);
   }
   
   @Test(expectedExceptions=RuntimeException.class) @SpecAssertion(section="3.5.3")
   public void testCannotHaveMultipleRemoveMethods()
   {
      RemoteComponentModel<Gibbon> gibbonModel = new RemoteComponentModel<Gibbon>(new SimpleAnnotatedType<Gibbon>(Gibbon.class), emptyAnnotatedItem, container);
   }
   
   @Test @SpecAssertion(section="3.5.3")
   public void testRemoveMethod()
   {
      RemoteComponentModel<TameOrangutan> tameOrangutanModel = new RemoteComponentModel<TameOrangutan>(new SimpleAnnotatedType<TameOrangutan>(TameOrangutan.class), emptyAnnotatedItem, container);
      assert tameOrangutanModel.getRemoveMethod().getMethod().getName().equals("removeOrangutan");
      RemoteComponentModel<Orangutan> orangutanModel = new RemoteComponentModel<Orangutan>(new SimpleAnnotatedType<Orangutan>(Orangutan.class), emptyAnnotatedItem, container);
      assert orangutanModel.getRemoveMethod().getMethod().getName().equals("removeOrangutan");
   }
   
   @Test @SpecAssertion(section="2.7.2")
   public void testStereotypeOnOtherInterface()
   {
	   assert false;
   }*/
   
}
