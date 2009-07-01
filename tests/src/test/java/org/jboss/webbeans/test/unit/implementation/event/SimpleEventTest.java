package org.jboss.webbeans.test.unit.implementation.event;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Current;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class SimpleEventTest extends AbstractWebBeansTest
{
   private static boolean called_flag_for_BindingType;
   private static boolean called_flag_for_NonBindingType;
   
   private static void initCalledFlag() {
      called_flag_for_BindingType = false;
      called_flag_for_NonBindingType = false;
   }
   
   @Test
   public void testEventUsingManager()
   {
      BeanManagerImpl manager = getCurrentManager();
      
      initCalledFlag();

      manager.fireEvent("Fired using Manager Interface with AnnotationLiteral.",
            new AnnotationLiteral<Updated>(){});

      assert called_flag_for_NonBindingType == true;
      assert called_flag_for_BindingType == true;
      
      initCalledFlag();
      
      manager.fireEvent("Fired using Manager Interface.");
      
      assert called_flag_for_NonBindingType == true;
      assert called_flag_for_BindingType == false; // not called
   }
   
   @Test
   public void testEventUsingEvent()
   {
      BeanManagerImpl manager = getCurrentManager();

      App app = manager.getInstanceByType(App.class);
      
      initCalledFlag();
      
//      app.fireEventByBindingDeclaredAtInjectionPoint();
//
//      assert called_flag_for_NonBindingType == true;
//      assert called_flag_for_BindingType == true;
      
      initCalledFlag();
      
      app.fireEventByAnnotationLiteral();
      
      assert called_flag_for_NonBindingType == true;
      assert called_flag_for_BindingType == true;
      
      initCalledFlag();
      
      app.fireEventNonBindingType();
      
      assert called_flag_for_NonBindingType == true;
      assert called_flag_for_BindingType == false; // not called
   }

   public static class App
   {
      @Any
      Event<String> event1;
      
      @Updated
      Event<String> event2;

      @Any
      Event<String> event3;

      public void fireEventByAnnotationLiteral()
      {
         event1.fire("Fired using Event Interface with AnnotationLiteral.",
               new AnnotationLiteral<Updated>(){});
      }
      
      public void fireEventByBindingDeclaredAtInjectionPoint()
      {
         event2.fire("Fired using Event Interface with Binding Declared.");
      }
      
      public void fireEventNonBindingType()
      {
         event3.fire("Fired using Event Interface with Non-BindingType.");
      }
   }

   public static class Receiver
   {
      public void receive1(@Observes @Updated String s)
      {
         called_flag_for_BindingType = true;
      }

      public void receive2(@Observes String s)
      {
         called_flag_for_NonBindingType = true;
      }
   }
}