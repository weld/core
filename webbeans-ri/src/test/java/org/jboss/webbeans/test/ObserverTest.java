package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.Observes;
import javax.webbeans.Observer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.AsynchronousAnnotationLiteral;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.test.mock.MockObserverImpl;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the implementation of Observer.
 * 
 * @author David Allen
 * 
 */
@SpecVersion("20081012")
public class ObserverTest
{
   private ManagerImpl manager;
   private SimpleComponentModel<Tuna> tuna;
   private InjectableMethod<?> om;

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

   @BeforeMethod
   public void before() throws Exception
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardAnnotationLiteral());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      manager = new MockContainerImpl(enabledDeploymentTypes);

      // Create an observer with known binding types
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousAnnotationLiteral());
      AnnotatedType<Tuna> annotatedItem = new SimpleAnnotatedType<Tuna>(Tuna.class, annotations);
      tuna = new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), annotatedItem, manager);
      om = new InjectableMethod<Object>(AnObserver.class.getMethod("observe", new Class[] { Event.class }));
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#getEventBindingTypes()}.
    */
   @SuppressWarnings("unchecked")
   @Test(groups = "eventbus") @SpecAssertion(section="7.3")
   public final void testGetEventBindingTypes() throws Exception
   {
      Observer<Event> o = new ObserverImpl<Event>(tuna, om, Event.class);
      //assert o.getEventBindingTypes().size() == 1;
      //assert Reflections.annotationSetMatches(o.getEventBindingTypes(), Asynchronous.class);
      //assert o.getEventType().equals(Event.class);
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#getEventType()}.
    * 
    * @throws
    * @throws Exception
    */
   @Test(groups = "eventbus") @SpecAssertion(section="7.3")
   public final void testGetEventType() throws Exception
   {
      Observer<Event> o = new ObserverImpl<Event>(tuna, om, Event.class);
      //assert o.getEventType().equals(Event.class);
   }

   /**
    * Test method for
    * {@link org.jboss.webbeans.event.ObserverImpl#notify(javax.webbeans.Container, java.lang.Object)}
    * .
    */
   @Test(groups = "eventbus") @SpecAssertion(section={"7.2","7.3"})
   public final void testNotify() throws Exception
   {
      AnObserver observerInstance = new AnObserver();
      Observer<Event> observer = new MockObserverImpl<Event>(tuna, om, Event.class);
      ((MockObserverImpl<Event>) observer).setInstance(observerInstance);
      Event event = new Event();
      observer.notify(event);
      assert observerInstance.notified;
   }

}
