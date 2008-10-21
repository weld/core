package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.Tame;
import org.jboss.webbeans.test.annotations.Synchronous;
import org.jboss.webbeans.test.bindings.AnimalStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.TameAnnotationLiteral;
import org.jboss.webbeans.test.bindings.SynchronousAnnotationLiteral;
import org.jboss.webbeans.test.components.DangerCall;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the implementation of an Event component.
 * 
 * @author David Allen
 * 
 */
@SpecVersion("20081012")
public class EventTest
{
   private MockContainerImpl manager = null;

   @BeforeMethod
   public void before() throws Exception
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardAnnotationLiteral());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      manager = new MockContainerImpl(enabledDeploymentTypes);
   }

   /**
    * Tests the {@link Event#fire(Object, Annotation...)} method with a locally
    * instantiated implementation.
    */
   @SuppressWarnings("unchecked")
   @Test(groups = "eventbus") @SpecAssertion(section="7.4")
   public void testFireEvent()
   {
      DangerCall anEvent = new DangerCall();
      // Create a test annotation for the event and use it to construct the
      // event object
      Annotation[] annotations = new Annotation[] { new AnimalStereotypeAnnotationLiteral() };
      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>();
      eventComponent.setEventBindings(annotations);
      eventComponent.setManager(manager);
      eventComponent.fire(anEvent, new TameAnnotationLiteral(),
            new SynchronousAnnotationLiteral());
      assert anEvent.equals(manager.getEvent());
      assert Reflections.annotationSetMatches(manager.getEventBindings(),
            AnimalStereotype.class, Tame.class,
            Synchronous.class);

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
      }
      catch (IllegalArgumentException e)
      {
         nonBindingTypeDetected = true;
      }
      assert nonBindingTypeDetected;
   }
}
