package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.Event;
import javax.webbeans.Observer;
import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.test.bindings.RoleBinding;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

/**
 * Temporary name until synchronized with David Allen
 * 
 * @author Nicklas Karlsson
 *
 */
@SpecVersion("PDR")
public class NewEventTest extends AbstractTest
{
   public static class AnEventType
   {
   }
   
   public static class AnObserver implements Observer<AnEventType>
   {

      public void notify(AnEventType event)
      {
      }
      
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void create() {
      SimpleBean<MyTest> myTestBean = BeanFactory.createSimpleBean(MyTest.class);
      for (AnnotatedField<Object> field : myTestBean.getEventFields()) {
         EventBean eventBean = BeanFactory.createEventBean(field.getType(), field);
         @SuppressWarnings("unused")
         Event<Param> event = eventBean.create();
      }
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.1")
   public void testEventTypeIncludesAllSuperclassesAndInterfacesOfEventObject() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.1")
   public void testEventObjectWithTypeVariablesFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.1")
   public void testEventObjectWithWildcardsFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.1")
   public void testConsumerNotifiedWhenEventTypeAndAllBindingsMatch() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.2")
   public void testManagerFireEvent() 
   {
      assert false;
   }
   
   @Test(groups={"events"})
   @SpecAssertion(section="7.3")
   public void testManagerAddObserver() 
   {
      Observer<AnEventType> observer = new AnObserver();
      
      // First test with the Class<T> of the event type
      manager.addObserver(observer, AnEventType.class);
      Set<Observer<AnEventType>> resolvedObservers = manager.resolveObservers(new AnEventType());
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 1;
      assert resolvedObservers.iterator().next() == observer;
      
      // Now test with the TypeLiteral<T> of the event type
      observer = new AnObserver();
      manager.addObserver(observer, new TypeLiteral<AnEventType>(){});
      resolvedObservers = manager.resolveObservers(new AnEventType());
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 2;
      boolean foundObserver = false;
      for (Observer<AnEventType> obs : resolvedObservers)
      {
         if (obs == observer)
         {
            foundObserver = true;
            break;
         }
      }
      assert foundObserver;
      
      // Try adding an observer with some binding types
      observer = new AnObserver();
      Annotation[] bindingTypes = new Annotation[] { new RoleBinding("Admin"), new RoleBinding("Manager") };
      manager.addObserver(observer, AnEventType.class, bindingTypes);
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindingTypes);
      assert !resolvedObservers.isEmpty();
      assert resolvedObservers.size() == 3;
      foundObserver = false;
      for (Observer<AnEventType> obs : resolvedObservers)
      {
         if (obs == observer)
         {
            foundObserver = true;
            break;
         }
      }
      assert foundObserver;
   }
    
   @Test(groups={"events"})
   @SpecAssertion(section="7.3")
   public void testManagerRemoveObserver() 
   {
      Observer<AnEventType> observer = new AnObserver();
      
      // First test with the Class<T> of the event type
      manager.addObserver(observer, AnEventType.class);
      manager.removeObserver(observer, AnEventType.class);
      Set<Observer<AnEventType>> resolvedObservers = manager.resolveObservers(new AnEventType());
      assert resolvedObservers.isEmpty();
      
      // Now test with the TypeLiteral<T> of the event type
      observer = new AnObserver();
      manager.addObserver(observer, new TypeLiteral<AnEventType>(){});
      manager.removeObserver(observer, new TypeLiteral<AnEventType>(){});
      resolvedObservers = manager.resolveObservers(new AnEventType());
      assert resolvedObservers.isEmpty();
      
      // Also test with binding types
      Annotation[] bindings = new Annotation[] { new RoleBinding("Admin") };
      manager.addObserver(observer, AnEventType.class, bindings);
      manager.removeObserver(observer, AnEventType.class);
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindings);
      assert !resolvedObservers.isEmpty();
      manager.removeObserver(observer, AnEventType.class, new RoleBinding("Admin"));
      resolvedObservers = manager.resolveObservers(new AnEventType(), bindings);
      assert resolvedObservers.isEmpty();
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.3")
   public void testMultipleInstancesOfSameBindingTypeWhenAddingObserverFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.3")
   public void testMultipleInstancesOfSameBindingTypeWhenRemovingObserverFails() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.3")
   public void testNonBindingTypePassedToRemoveObserverFails() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.4")
   public void testObserverThrowsExceptionAbortsNotifications() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.4")
   public void testObserverCalledBeforeTransactionCompleteMaySetRollbackOnly() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.4")
   public void testObserverManipulatingJTATransactionsDirectlyFails() 
   {
      assert false;
   }
   

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5")
   public void testStaticObserverMethodsFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5")
   public void testObserverMethodOnEnterpriseBeanIsBusinessMethod() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5")
   public void testMultipleObserverMethodsOK() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.1")
   public void testObserverMethodMustHaveOnlyOneEventParameter() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.1")
   public void testObserverMethodWithoutBindingTypesObservesEventsWithoutBindingTypes() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.1")
   public void testObserverMethodWithTypeVariablesFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.1")
   public void testObserverMethodWithWildcardsFails() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.2")
   public void testObserverMethodAnnotatedProducesFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.2")
   public void testObserverMethodAnnotatedInitializerFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.2")
   public void testObserverMethodAnnotatedDestructorFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.2")
   public void testObserverMethodHasDisposesAnnotatedParameterFails() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.3")
   public void testXMLDefinedObserverMethodIgnoresBindingAnnotations() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.3")
   public void testXMLDefinedObserverNotFindingImplementationMethodFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.4")
   public void testObserverMethodReceivesInjectionsOnNonObservesParameters() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.5")
   public void testConditionalObserver() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.1")
   public void testTransactionalObserverCanOnlyObserveSinglePhase() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.6")
   public void testTransactionalObserverNotifiedImmediatelyWhenNoTransactionInProgress() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.6")
   public void testAfterTransactionCompletionObserver() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.6")
   public void testAfterTransactionSuccessObserver() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.6")
   public void testAfterTransactionFailureObserver() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.6")
   public void testBeforeTransactionCompletionObserver() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.7")
   public void testObserverMethodRegistration() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.7")
   public void testEnterpriseBeanObserverMethodCalledWithCallerContext() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.7")
   public void testTransactionalObserverThrownExceptionIsCaughtAndLogged() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.7")
   public void testNonTransactionalObserverThrownNonCheckedExceptionIsRethrown() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.5.7")
   public void testNonTransactionalObserverThrownCheckedExceptionIsWrappedAndRethrown() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testDuplicateBindingsToFireFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testDuplicateBindingsToObservesFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testNonBindingTypePassedToFireFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testNonBindingTypePassedToObservesFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnField() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnParameterOfProducerMethod() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnParameterOfInitializerMethod() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnParameterOfDisposalMethod() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnParameterOfRemoveMethod() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnParameterOfConstructor() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableAnnotationOnNonEventTypeInjectionPointFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableannotationOnInjectionPointWithoutTypeParameterFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableannotationOnInjectionPointWithWildcardedTypeParameterFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObservableannotationOnInjectionPointWithTypeVariabledTypeParameterFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testImplicitObserverBeanMatchesAPITypeOfInectionPoint() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testImplicitObserverBeanMatchesBindingAnnotationsOfInjectionPoint() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testImplicitObserverBeanHasStandardDeploymentType() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testImplicitObserverBeanHasDependentScope() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testFireMethodCallsManagerFireWithEventObject() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testFireMethodCallsManagerFireWithBindingAnnotationsExceptObservable() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testFireMethodCallsManagerFireWithAllBindingAnnotationInstances() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObserveMethodCallsManagerAddObserverWithObserverObject() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationsExceptObservable() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.6")
   public void testObserveMethodCallsManagerAddObserverWithAllBindingAnnotationInstance() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testEventObjectContainsTypeVariablesWhenResolvingFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testEventObjectContainsWildcardsWhenResolvingFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testDuplicateBindingTypesWhenResolvingFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testNonBindingTypeAnnotationWhenResolvingFails() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testResolvingChecksEventType() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testResolvingChecksTypeParameters() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testResolvingChecksBindingTypes() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.7")
   public void testResolvingChecksBindingTypeMembers() 
   {
      assert false;
   }
   
}
