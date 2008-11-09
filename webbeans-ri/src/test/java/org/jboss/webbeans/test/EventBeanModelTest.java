package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.Standard;

import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.beans.DangerCall;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the bean model used only for the container supplied
 * Event bean.
 * 
 * @author David Allen
 *
 */
public class EventBeanModelTest
{
   private MockManagerImpl manager = null;
   //private EventBeanModel<EventImpl<DangerCall>> eventBeanModel = null;
   EventImpl<DangerCall> eventModelField = null;

   @BeforeMethod
   public void before() throws Exception
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl();
      manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
      Field eventModelField = this.getClass().getDeclaredField("eventModelField");
      /*eventBeanModel = new EventBeanModel<EventImpl<DangerCall>>(
            new SimpleAnnotatedField<EventImpl<DangerCall>>(eventModelField),
            new SimpleAnnotatedField<EventImpl<DangerCall>>(eventModelField),
            manager);*/

   }

   /**
    * The name should always be null since this type of bean is not allowed to have a name.
    */
   @Test(groups = "event")
   public void testName()
   {
      //assert eventBeanModel.getName() == null;
   }
   
   /**
    * The scope type should always be @Dependent
    */
   @Test(groups = "event")
   public void testScopeType()
   {
      //assert eventBeanModel.getScopeType().equals(Dependent.class);
   }
   
   /**
    * The deployment type should always be @Standard
    */
   @Test(groups = "event")
   public void testDeploymentType()
   {
      //assert eventBeanModel.getDeploymentType().equals(Standard.class);
   }
   
   @Test(groups = "event")
   public void testApiTypes()
   {
      //Set<Class<?>> apis = eventBeanModel.getApiTypes();
      //assert apis.size() >= 1;
      //for (Class<?> api : apis)
      //{
       //  api.equals(Event.class);
      //}
   }
   
   // TODO Fix this @Test(groups = "event")
   public void testConstructor()
   {
      /*BeanConstructor<EventImpl<DangerCall>, ?> constructor = eventBeanModel.getConstructor();
      assert constructor != null;
      Event<DangerCall> event = constructor.invoke(manager, null);
      assert event != null;*/
   }
}
