package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.webbeans.Current;
import javax.webbeans.Event;
import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;
import org.jboss.webbeans.model.EventComponentModel;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.FishStereotypeAnnotationLiteral;
import org.jboss.webbeans.test.bindings.RiverFishStereotypeAnnotationLiteral;
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
    * Tests the {@link Event#fire(Object, Annotation...)} method with a sample event
    * component.
    */
   @SuppressWarnings("unchecked")
   @Test(groups = "eventbus")
   public void testFireEvent()
   {
      DangerCall anEvent = new DangerCall();
      EventComponentModel<DangerCall> eventComponentModel = 
         new EventComponentModel<DangerCall>(
               new SimpleAnnotatedItem<Object>(new HashMap<Class<? extends Annotation>, Annotation>()),
               new SimpleAnnotatedItem<Object>(new HashMap<Class<? extends Annotation>, Annotation>()),
               manager);
      EventImpl<DangerCall> eventComponent = new EventImpl<DangerCall>(eventComponentModel);
      eventComponent.setManager(manager);
      eventComponent.fire(anEvent, new FishStereotypeAnnotationLiteral(), new RiverFishStereotypeAnnotationLiteral());
      assert anEvent.equals(manager.getEvent());
      assert Reflections.annotationSetMatches(manager.getEventBindings(), Current.class, FishStereotype.class, RiverFishStereotype.class);
   }
}
