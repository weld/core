package org.jboss.webbeans.test;

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
   public void testConsumerNotifiedWhenEventTypeAndAllBindingMathces() 
   {
      assert false;
   }

   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.2")
   public void testManagerFireEvent() 
   {
      assert false;
   }
   
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.3")
   public void testManagerAddObserver() 
   {
      assert false;
   }
    
   @Test(groups={"stub", "events"})
   @SpecAssertion(section="7.3")
   public void testManagerRemoveObserver() 
   {
      assert false;
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
   public void testObserverMethodRecievesInjectionsOnNonObservesParameters() 
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
