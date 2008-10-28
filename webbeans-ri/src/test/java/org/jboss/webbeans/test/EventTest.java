package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import javax.webbeans.Observer;
import javax.webbeans.Standard;

import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.beans.DangerCall;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the implementation of an Event component.
 * 
 * @author David Allen
 * 
 */
@SpecVersion("PDR")
public class EventTest
{
   private MockManagerImpl manager = null;

   /**
    * Test class used as an observer.
    * 
    */
   public class AnObserver<T> implements Observer<T>
   {
      protected boolean notified = false;

      public void notify(T event)
      {
         this.notified = true;
      }
   }

   @BeforeMethod
   public void before() throws Exception
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl(enabledDeploymentTypes);
   }

   /**
    * Tests the {@link Event#fire(Object, Annotation...)} method with a locally
    * instantiated implementation.
    */
   @SuppressWarnings("unchecked")
   @Test(groups = "event")
   @SpecAssertion(section = "7.6")
   public void testFireEvent()
   {
      DangerCall anEvent = new DangerCall();
      // Create a test annotation for the event and use it to construct the
      // event object
      Annotation[] annotations = new Annotation[] { new TameAnnotationLiteral() };
      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>();
      eventComponent.setEventBindings(annotations);
      eventComponent.setManager(manager);
      eventComponent.fire(anEvent, new SynchronousAnnotationLiteral());
      assert anEvent.equals(manager.getEvent());
      assert Reflections.annotationSetMatches(manager.getEventBindings(),
            Tame.class, Synchronous.class);

      // Test duplicate annotations on the fire method call
      boolean duplicateDetected = false;
      try
      {
         eventComponent.fire(anEvent, new TameAnnotationLiteral(),
               new TameAnnotationLiteral());
      } catch (DuplicateBindingTypeException e)
      {
         duplicateDetected = true;
      }
      assert duplicateDetected;

      // Test annotations that are not binding types
      boolean nonBindingTypeDetected = false;
      try
      {
         eventComponent.fire(anEvent, new FishStereotypeAnnotationLiteral());
      } catch (IllegalArgumentException e)
      {
         nonBindingTypeDetected = true;
      }
      assert nonBindingTypeDetected;
   }

   /**
    * Tests the {@link Event#observe(javax.webbeans.Observer, Annotation...)}
    * method with a locally instantiated implementation.
    */
   @Test(groups = "event")
   @SpecAssertion(section = "7.6")
   public void testObserve()
   {
      // Create a test annotation for the event and use it to construct the
      // event object
      Annotation[] annotations = new Annotation[] { new TameAnnotationLiteral() };
      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>();
      eventComponent.setEventBindings(annotations);
      eventComponent.setManager(manager);
      Observer<DangerCall> observer = new AnObserver<DangerCall>();
      eventComponent.observe(observer, new SynchronousAnnotationLiteral());
      assert manager.getEventType().equals(DangerCall.class);

      // Try duplicate annotation bindings
      boolean duplicateDetected = false;
      try
      {
         eventComponent.observe(observer,
               new TameAnnotationLiteral());
      } catch (DuplicateBindingTypeException e)
      {
         duplicateDetected = true;
      }
      assert duplicateDetected;

      // Try an invalid binding type
      boolean nonBindingTypeDetected = false;
      try
      {
         eventComponent.observe(observer,
               new RiverFishStereotypeAnnotationLiteral());
      } catch (IllegalArgumentException e)
      {
         nonBindingTypeDetected = true;
      }
      assert nonBindingTypeDetected;
   }

}
