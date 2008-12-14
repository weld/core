package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.util.Arrays;

import javax.webbeans.Observes;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.beans.Tuna;
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
   
   @Override
   protected void addStandardDeploymentTypesForTests()
   {
      manager.setEnabledDeploymentTypes(Arrays.asList(Standard.class, AnotherDeploymentType.class));
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
   @Test(groups={"deferredEvent", "broken"})
   public final void testBeforeCompletion() throws Exception
   {
      // When the transaction is committed, the beforeCompletion() method is
      // invoked which in turn invokes the observer. Here the mock observer
      // is used to keep track of the event being fired.
      SimpleBean<Tuna> tuna;
      AnnotatedMethod<Object> om;
      

      // Create an observer with known binding types
      //Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      //annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      //AnnotatedClass<Tuna> annotatedItem = new SimpleAnnotatedClass<Tuna>(Tuna.class, annotations);
      // TODO This should test a real class
      tuna = createSimpleBean(Tuna.class, manager);
      om = new AnnotatedMethodImpl<Object>(AnObserver.class.getMethod("observe", new Class[] { Event.class }), new AnnotatedClassImpl<AnObserver>(AnObserver.class));

      AnObserver observerInstance = new AnObserver();
      // TODO Fix this Observer<Event> observer = new MockObserverImpl<Event>(tuna, om, Event.class);
      //((MockObserverImpl<Event>) observer).setInstance(observerInstance);
      Event event = new Event();
      //DeferredEventNotification<Event> deferredNotification = new DeferredEventNotification<Event>(event, observer);
      //deferredNotification.beforeCompletion();
      assert observerInstance.notified;
   }

}
