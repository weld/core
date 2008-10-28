package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.event.DeferredEventNotification;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.test.mock.MockObserverImpl;
import org.testng.annotations.Test;

/**
 * Unit tests for the deferred event notification object used to delay
 * notification till the end of a transaction.
 * 
 * @author David Allen
 * 
 */
public class DeferredEventNotificationTest extends AbstractTest
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
   @Test(groups="deferredEvent")
   public final void testBeforeCompletion() throws Exception
   {
      // When the transaction is committed, the beforeCompletion() method is
      // invoked which in turn invokes the observer. Here the mock observer
      // is used to keep track of the event being fired.
      ManagerImpl manager;
      SimpleBeanModel<Tuna> tuna;
      InjectableMethod<Object> om;
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl(enabledDeploymentTypes);

      // Create an observer with known binding types
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, annotations);
      tuna = new SimpleBeanModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      om = new InjectableMethod<Object>(AnObserver.class.getMethod("observe", new Class[] { Event.class }));

      AnObserver observerInstance = new AnObserver();
      Observer<Event> observer = new MockObserverImpl<Event>(tuna, om, Event.class);
      ((MockObserverImpl<Event>) observer).setInstance(observerInstance);
      Event event = new Event();
      DeferredEventNotification<Event> deferredNotification = new DeferredEventNotification<Event>(event, observer);
      deferredNotification.beforeCompletion();
      assert observerInstance.notified;
   }

}
