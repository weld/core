/**
 * 
 */
package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Production;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.bindings.StandardBinding;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.event.ObserverMethod;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.Asynchronous;
import org.jboss.webbeans.test.annotations.FishStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.RiverFishStereotype;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeBinding;
import org.jboss.webbeans.test.bindings.AsynchronousBinding;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.jboss.webbeans.util.Reflections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the implementation of Observer.
 * 
 * @author David Allen
 *
 */
public class ObserverTest
{
   private ContainerImpl container;
   
   public class Event
   {
      // Simple class used for testing
   }
   
   public class AnObserver
   {
      public void observe(@Observes Event e)
      {
         // An observer method
      }
   }

   @BeforeMethod
   public void before()
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardBinding());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeBinding());
      container = new MockContainerImpl(enabledDeploymentTypes);
      
      initStereotypes(container);
   }
   
   private void initStereotypes(ContainerImpl container)
   {
      container.getModelManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(AnimalStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(FishStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(RiverFishStereotype.class)));
      container.getModelManager().addStereotype(new StereotypeModel(new SimpleAnnotatedType(RequestScopedAnimalStereotype.class)));
   }
   

   /**
    * Test method for {@link org.jboss.webbeans.event.ObserverImpl#getEventBindingTypes()}.
    */
   @SuppressWarnings("unchecked")
   @Test(groups="eventbus")
   public final void testGetEventBindingTypes() throws Exception
   {
      // Create an observer with known binding types
      Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
      annotations.put(Asynchronous.class, new AsynchronousBinding());
      AnnotatedType annotatedItem = new SimpleAnnotatedType(Tuna.class, annotations);
      SimpleComponentModel<Tuna> tuna = new SimpleComponentModel<Tuna>(new SimpleAnnotatedType(Tuna.class), annotatedItem, container);
      assert tuna.getDeploymentType().annotationType().equals(Production.class);
      ObserverMethod om = new ObserverMethod(AnObserver.class.getMethod("observe", new Class[]{Event.class}));

      Observer<Event> o = new ObserverImpl<Event>(tuna, om, Event.class);
      assert o.getEventBindingTypes().size() == 1;
      assert Reflections.annotationSetMatches(o.getEventBindingTypes(), Asynchronous.class);
      assert o.getEventType().equals(Event.class);
   }

   /**
    * Test method for {@link org.jboss.webbeans.event.ObserverImpl#getEventType()}.
    */
   @Test(groups="eventbus")
   public final void testGetEventType()
   {
      //TODO Implement
   }

   /**
    * Test method for {@link org.jboss.webbeans.event.ObserverImpl#notify(javax.webbeans.Container, java.lang.Object)}.
    */
   @Test(groups="eventbus")
   public final void testNotify()
   {
      //TODO Implement
   }

}
