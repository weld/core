package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Event;
import javax.webbeans.Standard;

import org.jboss.webbeans.bindings.StandardAnnotationLiteral;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;
import org.jboss.webbeans.model.EventComponentModel;
import org.jboss.webbeans.test.bindings.AnotherDeploymentTypeAnnotationLiteral;
import org.jboss.webbeans.test.mock.MockContainerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the component model used only for the container supplied
 * Event component.
 * 
 * @author David Allen
 *
 */
public class EventComponentModelTest
{
   private MockContainerImpl manager = null;
   private EventComponentModel<Event<? extends Object>> eventComponentModel = null;

   @BeforeMethod
   public void before() throws Exception
   {
      List<Annotation> enabledDeploymentTypes = new ArrayList<Annotation>();
      enabledDeploymentTypes.add(new StandardAnnotationLiteral());
      enabledDeploymentTypes.add(new AnotherDeploymentTypeAnnotationLiteral());
      manager = new MockContainerImpl(enabledDeploymentTypes);
      eventComponentModel = new EventComponentModel<Event<? extends Object>>(
            new SimpleAnnotatedItem<Object>(
                  new HashMap<Class<? extends Annotation>, Annotation>()),
            new SimpleAnnotatedItem<Object>(
                  new HashMap<Class<? extends Annotation>, Annotation>()),
            manager);

   }

   /**
    * The name should always be null since this type of component is not allowed to have a name.
    */
   @Test(groups = "eventbus")
   public void testName()
   {
      assert eventComponentModel.getName() == null;
   }
   
   /**
    * The scope type should always be @Dependent
    */
   @Test(groups = "eventbus")
   public void testScopeType()
   {
      assert eventComponentModel.getScopeType().annotationType().equals(Dependent.class);
   }
   
   /**
    * The deployment type should always be @Standard
    */
   @Test(groups = "eventbus")
   public void testDeploymentType()
   {
      assert eventComponentModel.getDeploymentType().annotationType().equals(Standard.class);
   }
   
   @Test(groups = "eventbus")
   public void testApiTypes()
   {
      Set<Class> apis = eventComponentModel.getApiTypes();
      assert apis.size() >= 1;
      for (Class api : apis)
      {
         api.equals(Event.class);
      }
   }
   
   @Test(groups = "eventbus")
   public void testConstructor()
   {
      ComponentConstructor<Event<? extends Object>> constructor = eventComponentModel.getConstructor();
      assert constructor != null;
      Event<? extends Object> event = constructor.invoke(manager);
      assert event != null;
   }
}
