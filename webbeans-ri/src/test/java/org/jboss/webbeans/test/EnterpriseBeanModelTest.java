package org.jboss.webbeans.test;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;
import javax.webbeans.UnremovedException;

import org.testng.annotations.Test;

@SuppressWarnings( { "unchecked", "unused" })
@SpecVersion("PDR")
public class EnterpriseBeanModelTest extends AbstractTest
{

   @Test
   @SpecAssertion(section = "3.3")
   public void testStatelessWithDependentScopeOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEJBInterceptorFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEJBDecoratorFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testMultipleAnnotationDefinedEJBsWithSameImplementationClassFails()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testMultipleXMLDefinedEJBsWithSameImplementationClassOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DeploymentException.class)
   @SpecAssertion(section = "3.3")
   public void testMultipleEnabledSpecializedEJBFails()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEJB()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEJBComplementedWithXML()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEJB()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEJBComplementedWithXML()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testAPITypesAreLocalInterfacesWithoutWildcardTypesOrTypeVariablesWithSuperInterfaces()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testEJBWithLocalViewAndParameterizedTypeIncludesBeanClassAndSuperclassesInAPITypes()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testObjectIsInAPITypes()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testRemoteInterfacesAreNotInAPITypes()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedEJBIgnoresProducerAndDisposalAndObserverAnnotations()
   {

   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedSingletonsFail()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.4")
   public void testMessageDrivenBeansNotOK()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBRemoveMethodCalledOnDestroy()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodInDependentScopeOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodInApplicationScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodInSessionScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodInConversationScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodInRequestScopeFails()
   {
      assert false;
   }

   @Test(expectedExceptions = UnremovedException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEJBWithoutRemoveMethodNotExplicitlyDestroyedBeforeManagerAttemptFails()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testNoParametersPassedWhenEJBRemoveMethodCalledFromApplication()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testWebBeanRemoveMethodCallRemovesInstanceFromContext()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEJBWithoutDestructorUsesRemoveMethodWithoutParamsAsWebBeansRemoveMethod()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEJBWithoutDestructorAndRemoveMethodWithoutParamsHasNoWebBeansRemoveMethod()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEJBWithMultipleDestructorAnnotationsFail()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEJBWithDestructorAnnotationOnMethodNotAnEJBRemoveMethodFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEJBWithInitializerAnnotationOnRemoveMethodFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEJBWithProducerAnnotationOnRemoveMethodFails()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEJBWithRemoveMethodTakingObserverAnnotatedParameterFails()
   {
      assert false;
   }

   // TODO Separate section for XML defined beans?
   
   @Test
   @SpecAssertion(section = "3.3.5.3")
   public void testRemoveMethodParameters()
   {
      assert false;
   }
   
   
/*   
   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void test()
   {
      assert false;
   }
/*   
   
   /*
    * @Test public void testStateless() { EnterpriseBeanModel<Lion> lion = new
    * EnterpriseBeanModel<Lion>(new SimpleAnnotatedType<Lion>(Lion.class),
    * getEmptyAnnotatedType(Lion.class), manager); assert
    * lion.getScopeType().equals(Dependent.class);
    * Reflections.annotationSetMatches(lion.getBindingTypes(), Current.class);
    * assert lion.getName().equals("lion"); }
    * 
    * @Test @SpecAssertion(section="3.3") public void
    * testStatelessWithDependentScopeOK() { EnterpriseBeanModel<Giraffe> giraffe
    * = Util.createEnterpriseBeanModel(Giraffe.class, manager); assert
    * giraffe.getScopeType().equals(Dependent.class); assert
    * Reflections.annotationSetMatches(giraffe.getBindingTypes(),
    * Current.class); }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testStatelessWithRequestScopeFails() { EnterpriseBeanModel<Bear> bear =
    * Util.createEnterpriseBeanModel(Bear.class, manager); }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testStatelessWithApplicationScopeFails() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testStatelessWithSessionScopeFails() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testStatelessWithConversationScopeFails() { assert false; }
    * 
    * public void testSingletonWithDependentScopeOK() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testSingletonWithRequestScopeFails() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testSingletonWithApplicationScopeOK() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testSingletonWithSessionScopeFails() { assert false; }
    * 
    * @Test(expectedExceptions = RuntimeException.class)
    * @SpecAssertion(section="3.3") public void
    * testSingletonWithConversationScopeFails() { assert false; }
    * 
    * @Test public void testStateful() { EnterpriseBeanModel<Tiger> tiger =
    * Util.createEnterpriseBeanModel(Tiger.class, manager); assert
    * Reflections.annotationSetMatches(tiger.getBindingTypes(),
    * Synchronous.class); assert
    * tiger.getRemoveMethod().getAnnotatedItem().getDelegate
    * ().getName().equals("remove"); assert tiger.getName() == null; }
    * 
    * @Test public void testMultipleRemoveMethodsWithDestroys() {
    * EnterpriseBeanModel<Elephant> elephant =
    * Util.createEnterpriseBeanModel(Elephant.class, manager); assert
    * elephant.getRemoveMethod
    * ().getAnnotatedItem().getDelegate().getName().equals("remove2"); }
    * 
    * @Test(expectedExceptions=RuntimeException.class) public void
    * testMultipleRemoveMethodsWithoutDestroys() { EnterpriseBeanModel<Puma>
    * puma = Util.createEnterpriseBeanModel(Puma.class, manager); }
    * 
    * @Test(expectedExceptions=RuntimeException.class) public void
    * testMultipleRemoveMethodsWithMultipleDestroys() {
    * EnterpriseBeanModel<Cougar> cougar =
    * Util.createEnterpriseBeanModel(Cougar.class, manager); }
    * 
    * @Test(expectedExceptions=RuntimeException.class) public void
    * testNonStatefulEnterpriseBeanWithDestroys() { EnterpriseBeanModel<Cheetah>
    * cheetah = Util.createEnterpriseBeanModel(Cheetah.class, manager); }
    * 
    * @Test public void testRemoveMethodWithDefaultBinding() {
    * EnterpriseBeanModel<Panther> panther =
    * Util.createEnterpriseBeanModel(Panther.class, manager); assert
    * panther.getRemoveMethod
    * ().getAnnotatedItem().getDelegate().getName().equals("remove"); assert
    * panther.getRemoveMethod().getParameters().size() == 1; assert
    * panther.getRemoveMethod
    * ().getParameters().get(0).getType().equals(String.class); assert
    * panther.getRemoveMethod().getParameters().get(0).getBindingTypes().size()
    * == 1; assert
    * Reflections.annotationSetMatches(panther.getRemoveMethod().getParameters
    * ().get(0).getBindingTypes(), Current.class); }
    * 
    * @Test public void testMessageDriven() { EnterpriseBeanModel<Leopard>
    * leopard = Util.createEnterpriseBeanModel(Leopard.class, manager); assert
    * Reflections.annotationSetMatches(leopard.getBindingTypes(),
    * Current.class); }
    */
}
