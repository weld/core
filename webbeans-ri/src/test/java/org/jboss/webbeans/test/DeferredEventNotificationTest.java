package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.manager.Observer;
import javax.webbeans.Observes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.event.DeferredEventNotification;
import org.jboss.webbeans.event.ObserverMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.test.mock.MockObserverImpl;
import org.testng.annotations.Test;

/**
 * Unit tests for the deferred event notification object used to delay
 * notification till the end of a transaction.
 * 
 * @author David Allen
 * 
 */
public class DeferredEventNotificationTest
{

   public class Event
   {
      // Simple class used for testing
   }

   public class AnObserver
   {
      protected boolean notified = false;

      public void observe(@Observes @Asynchronous Event e)
      {
         // An observer method
         this.notified = true;
      }
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.DeferredEventNotification#beforeCompletion()}
    * .
    */
   @Test
   public final void testBeforeCompletion() throws Exception
   {
      // When the transaction is committed, the beforeCompletion() method is
      // invoked which in turn invokes the observer. Here the mock observer
      // is used to keep track of the event being fired.
      ManagerImpl manager;
      SimpleComponentModel<Tuna> tuna;
      ObserverMethod om;
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardAnnotationLiteral());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      manager = new MockContainerImpl(enabledDeploymentTypes);

      // Create an observer with known binding types
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, annotations);
      tuna = new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      om = new ObserverMethod(AnObserver.class.getMethod("observe", new Class[] { Event.class }));

      AnObserver observerInstance = new AnObserver();
      Observer<Event> observer = new MockObserverImpl<Event>(tuna, om, Event.class);
      ((MockObserverImpl<Event>) observer).setInstance(observerInstance);
      Event event = new Event();
      DeferredEventNotification deferredNotification = new DeferredEventNotification(manager, event, observer);
      deferredNotification.beforeCompletion();
      assert observerInstance.notified;
   }

}
