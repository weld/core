package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Observer;
import javax.webbeans.Standard;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;

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

   @SuppressWarnings("unchecked")
   @BeforeMethod
   public void before() throws Exception
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl();
      manager.setEnabledDeploymentTypes(Standard.class, AnotherDeploymentType.class);
   }

//   /**
//    * Tests the {@link Event#fire(Object, Annotation...)} method with a locally
//    * instantiated implementation.
//    */
//   @SuppressWarnings("unchecked")
//   @Test(groups = "event")
//   @SpecAssertion(section = "7.6")
//   public void testFireEvent()
//   {
//      DangerCall anEvent = new DangerCall();
//      // Create a test annotation for the event and use it to construct the
//      // event object
//      Annotation[] annotations = new Annotation[] { new TameAnnotationLiteral() };
//      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>();
//      eventComponent.setEventBindings(annotations);
//      eventComponent.setManager(manager);
//      eventComponent.fire(anEvent, new SynchronousAnnotationLiteral());
//      assert anEvent.equals(manager.getEvent());
//      assert Reflections.annotationSetMatches(manager.getEventBindings(),
//            Tame.class, Synchronous.class);
//
//      // Test duplicate annotations on the fire method call
//      boolean duplicateDetected = false;
//      try
//      {
//         eventComponent.fire(anEvent, new TameAnnotationLiteral(),
//               new TameAnnotationLiteral());
//      } catch (DuplicateBindingTypeException e)
//      {
//         duplicateDetected = true;
//      }
//      assert duplicateDetected;
//
//      // Test annotations that are not binding types
//      boolean nonBindingTypeDetected = false;
//      try
//      {
//         eventComponent.fire(anEvent, new FishStereotypeAnnotationLiteral());
//      } catch (IllegalArgumentException e)
//      {
//         nonBindingTypeDetected = true;
//      }
//      assert nonBindingTypeDetected;
//   }

//   /**
//    * Tests the {@link Event#observe(javax.webbeans.Observer, Annotation...)}
//    * method with a locally instantiated implementation.
//    */
//   @Test(groups = {"observerMethod"})
//   @SpecAssertion(section = "7.6")
//   public void testObserve()
//   {
//      // Create a test annotation for the event and use it to construct the
//      // event object
//      Annotation[] annotations = new Annotation[] { new TameAnnotationLiteral() };
//      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>();
//      eventComponent.setEventType(DangerCall.class);
//      eventComponent.setEventBindings(annotations);
//      eventComponent.setManager(manager);
//      Observer<DangerCall> observer = new AnObserver<DangerCall>();
//      eventComponent.observe(observer, new SynchronousAnnotationLiteral());
//      assert manager.getEventType().equals(DangerCall.class);
//
//      // Try duplicate annotation bindings
//      boolean duplicateDetected = false;
//      try
//      {
//         eventComponent.observe(observer,
//               new TameAnnotationLiteral());
//      } catch (DuplicateBindingTypeException e)
//      {
//         duplicateDetected = true;
//      }
//      assert duplicateDetected;
//
//      // Try an invalid binding type
//      boolean nonBindingTypeDetected = false;
//      try
//      {
//         eventComponent.observe(observer,
//               new RiverFishStereotypeAnnotationLiteral());
//      } catch (IllegalArgumentException e)
//      {
//         nonBindingTypeDetected = true;
//      }
//      assert nonBindingTypeDetected;
//   }

}
