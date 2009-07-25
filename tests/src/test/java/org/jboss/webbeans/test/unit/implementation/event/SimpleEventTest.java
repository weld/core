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
   private static boolean RECEIVE_1_OBSERVED;
   private static boolean RECEIVE_2_OBSERVED;
   private static boolean RECEIVE_3_OBSERVED;
   
   private static void initFlags() {
      RECEIVE_1_OBSERVED = false;
      RECEIVE_2_OBSERVED = false;
      RECEIVE_3_OBSERVED = false;
   }
   
   @Test
   public void testFireEventOnManager()
   {
      BeanManagerImpl manager = getCurrentManager();
      
      initFlags();

      manager.fireEvent("Fired using Manager Interface with AnnotationLiteral.", new AnnotationLiteral<Updated>(){});

      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_1_OBSERVED == true;
      assert RECEIVE_3_OBSERVED == false;
      
      initFlags();
      
      manager.fireEvent("Fired using Manager Interface.");
      
      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_1_OBSERVED == false; // not called
      assert RECEIVE_3_OBSERVED == true;
   }
   
   @Test
   public void testFireEventOnEvent()
   {
      BeanManagerImpl manager = getCurrentManager();

      App app = createContextualInstance(App.class);
      
      initFlags();
      
      app.fireEventByBindingDeclaredAtInjectionPoint();

      assert RECEIVE_1_OBSERVED == true;
      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_3_OBSERVED == false;
      
      initFlags();
      
      app.fireEventByAnnotationLiteral();
      
      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_1_OBSERVED == true;
      assert RECEIVE_3_OBSERVED == false;
      
      initFlags();
      
      app.fireEventViaAny();
      
      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_1_OBSERVED == false; // not called
      assert RECEIVE_3_OBSERVED == false;
      
      initFlags();
      
      app.fireEventViaCurrent();
      
      assert RECEIVE_2_OBSERVED == true;
      assert RECEIVE_1_OBSERVED == false; // not called
      assert RECEIVE_3_OBSERVED == true;
   }

   public static class App
   {
      @Any
      Event<String> event1;
      
      @Updated
      Event<String> event2;

      @Any
      Event<String> event3;
      
      @Current
      Event<String> event4;

      public void fireEventByAnnotationLiteral()
      {
         event1.select(new AnnotationLiteral<Updated>(){}).fire("Fired using Event Interface with AnnotationLiteral.");
      }
      
      public void fireEventByBindingDeclaredAtInjectionPoint()
      {
         event2.fire("Fired using Event Interface with Binding Declared.");
      }
      
      public void fireEventViaAny()
      {
         event3.fire("Fired using Event Interface with Non-BindingType.");
      }
      
      public void fireEventViaCurrent()
      {
         event4.fire("Fired using Event Interface with @Current");
      }
   }

   public static class Receiver
   {
      public void receive1(@Observes @Updated String s)
      {
         RECEIVE_1_OBSERVED = true;
      }

      public void receive2(@Any @Observes String s)
      {
         RECEIVE_2_OBSERVED = true;
      }
      
      public void receive3(@Observes String s)
      {
         RECEIVE_3_OBSERVED = true;
      }
   }
}