package org.jboss.webbeans.test;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;
import javax.webbeans.UnremovedException;

import org.jboss.webbeans.model.bean.EnterpriseBeanModel;
import org.jboss.webbeans.test.beans.Giraffe;
import org.jboss.webbeans.test.beans.GreatDane;
import org.jboss.webbeans.test.beans.Laika;
import org.jboss.webbeans.test.beans.Leopard;
import org.jboss.webbeans.test.beans.Pitbull;
import org.jboss.webbeans.test.beans.broken.Armant;
import org.jboss.webbeans.test.beans.broken.Beagle;
import org.jboss.webbeans.test.beans.broken.Boxer;
import org.jboss.webbeans.test.beans.broken.Bullmastiff;
import org.jboss.webbeans.test.beans.broken.Dachshund;
import org.jboss.webbeans.test.beans.broken.GoldenRetriever;
import org.jboss.webbeans.test.beans.broken.Greyhound;
import org.jboss.webbeans.test.beans.broken.Husky;
import org.jboss.webbeans.test.beans.broken.IrishTerrier;
import org.jboss.webbeans.test.beans.broken.JackRussellTerrier;
import org.jboss.webbeans.test.beans.broken.Pekingese;
import org.jboss.webbeans.test.beans.broken.Poodle;
import org.jboss.webbeans.test.beans.broken.Pug;
import org.jboss.webbeans.test.beans.broken.Pumi;
import org.jboss.webbeans.test.beans.broken.Rottweiler;
import org.jboss.webbeans.test.beans.broken.RussellTerrier;
import org.jboss.webbeans.test.beans.broken.Saluki;
import org.jboss.webbeans.test.beans.broken.Spitz;
import org.jboss.webbeans.test.beans.broken.Toller;
import org.jboss.webbeans.test.beans.broken.WelshCorgie;
import org.jboss.webbeans.test.beans.broken.Whippet;
import org.jboss.webbeans.test.util.Util;
import org.testng.annotations.Test;

@SuppressWarnings( { "unchecked", "unused" })
@SpecVersion("PDR")
public class EnterpriseBeanModelTest extends AbstractTest
{

   @Test
   @SpecAssertion(section = "3.3")
   public void testStatelessWithDependentScopeOK()
   {
      EnterpriseBeanModel<Giraffe> giraffe = Util.createEnterpriseBeanModel(Giraffe.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      EnterpriseBeanModel<Beagle> beagle = Util.createEnterpriseBeanModel(Beagle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      EnterpriseBeanModel<Boxer> boxer = Util.createEnterpriseBeanModel(Boxer.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      EnterpriseBeanModel<Bullmastiff> boxer = Util.createEnterpriseBeanModel(Bullmastiff.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      EnterpriseBeanModel<Dachshund> dachshund = Util.createEnterpriseBeanModel(Dachshund.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      EnterpriseBeanModel<GreatDane> greatDane = Util.createEnterpriseBeanModel(GreatDane.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      EnterpriseBeanModel<Greyhound> greyhound = Util.createEnterpriseBeanModel(Greyhound.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      EnterpriseBeanModel<Husky> husky = Util.createEnterpriseBeanModel(Husky.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      EnterpriseBeanModel<IrishTerrier> irishTerrier = Util.createEnterpriseBeanModel(IrishTerrier.class, manager);
   }

   @Test
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanInterceptorFails()
   {
      EnterpriseBeanModel<Pug> pug = Util.createEnterpriseBeanModel(Pug.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanDecoratorFails()
   {
      EnterpriseBeanModel<Pekingese> pekingese = Util.createEnterpriseBeanModel(Pekingese.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class, groups="enterpriseBeans")
   @SpecAssertion(section = "3.3")
   public void testMultipleAnnotationDefinedEnterpriseBeansWithSameImplementationClassFails()
   {
      assert false;
   }

   @Test(groups={"webbeansxml", "enterpriseBeans"})
   @SpecAssertion(section = "3.3")
   public void testMultipleXMLDefinedEnterpriseBeansWithSameImplementationClassOK()
   {
      assert false;
   }

   @Test(expectedExceptions = DeploymentException.class, groups={"enterpriseBeans", "specialization"})
   @SpecAssertion(section = "3.3")
   public void testMultipleEnabledSpecializedEnterpriseBeanFails()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBean()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "ejbjarxml"})
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBeanComplementedWithXML()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testAPITypesAreLocalInterfacesWithoutWildcardTypesOrTypeVariablesWithSuperInterfaces()
   {
      assert false;
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testEnterpriseBeanWithLocalViewAndParameterizedTypeIncludesBeanClassAndSuperclassesInAPITypes()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.2")
   public void testObjectIsInAPITypes()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
      assert laika.getApiTypes().contains(Object.class);
   }

   @Test(groups="enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testRemoteInterfacesAreNotInAPITypes()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "producerMethod", "observerMethod", "renoveMethod", "webbeansxml"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedEnterpriseBeanIgnoresProducerAndDisposalAndObserverAnnotations()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class, groups={"enterpriseBeans", "ejbjarxml", "singletons"})
   @SpecAssertion(section = "3.3.4")
   public void testXMLDefinedSingletonsFail()
   {
      assert false;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testMessageDrivenBeansNotOK()
   {
      EnterpriseBeanModel<Leopard> Leopard = Util.createEnterpriseBeanModel(Leopard.class, manager);
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanRemoveMethodCalledOnDestroy()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInDependentScopeOK()
   {
      EnterpriseBeanModel<Pitbull> pitbull = Util.createEnterpriseBeanModel(Pitbull.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInApplicationScopeFails()
   {
      EnterpriseBeanModel<Poodle> poodle = Util.createEnterpriseBeanModel(Poodle.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInSessionScopeFails()
   {
      EnterpriseBeanModel<Rottweiler> rottweiler = Util.createEnterpriseBeanModel(Rottweiler.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInConversationScopeFails()
   {
      EnterpriseBeanModel<Spitz> spitz = Util.createEnterpriseBeanModel(Spitz.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodInRequestScopeFails()
   {
      EnterpriseBeanModel<Saluki> saluki = Util.createEnterpriseBeanModel(Saluki.class, manager);
   }

   @Test(expectedExceptions = UnremovedException.class, groups={"enterpriseMethods", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodNotExplicitlyDestroyedBeforeManagerAttemptFails()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testNoParametersPassedWhenEnterpriseBeanRemoveMethodCalledFromApplication()
   {
      assert false;
   }

   @Test(groups={"enterpriseBeans", "removeMethod"})
   @SpecAssertion(section = "3.3.5")
   public void testWebBeanRemoveMethodCallRemovesInstanceFromContext()
   {
      assert false;
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorUsesRemoveMethodWithoutParamsAsWebBeansRemoveMethod()
   {
      EnterpriseBeanModel<Armant> whippet = Util.createEnterpriseBeanModel(Armant.class, manager);
      assert whippet.getRemoveMethod().getAnnotatedItem().getDelegate().getName().equals("destroy");
   }

   @Test
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithoutDestructorAndRemoveMethodWithoutParamsHasNoWebBeansRemoveMethod()
   {
      EnterpriseBeanModel<Laika> laika = Util.createEnterpriseBeanModel(Laika.class, manager);
      assert laika.getRemoveMethod() == null;
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithMultipleDestructorAnnotationsFail()
   {
      EnterpriseBeanModel<Whippet> whippet = Util.createEnterpriseBeanModel(Whippet.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorAnnotationOnMethodNotAnEJBRemoveMethodFails()
   {
      EnterpriseBeanModel<GoldenRetriever> goldenRetriever = Util.createEnterpriseBeanModel(GoldenRetriever.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testAnnotationDefinedEnterpriseBeanWithDestructorWithDisposesParameterFails()
   {
      EnterpriseBeanModel<Pumi> pumi = Util.createEnterpriseBeanModel(Pumi.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithInitializerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBeanModel<JackRussellTerrier> jackRussellTerrier = Util.createEnterpriseBeanModel(JackRussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithProducerAnnotationOnRemoveMethodFails()
   {
      EnterpriseBeanModel<RussellTerrier> russellTerrier = Util.createEnterpriseBeanModel(RussellTerrier.class, manager);
   }

   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5.1")
   public void testEnterpriseBeanWithRemoveMethodTakingObserverAnnotatedParameterFails()
   {
      EnterpriseBeanModel<Toller> toller = Util.createEnterpriseBeanModel(Toller.class, manager);
   }

   // TODO Separate section for XML defined beans?
   
   @Test(groups={"removeMethod", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.5.3")
   public void testRemoveMethodParameters()
   {
      assert false;
   }
   
   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotExtendingSpecializedBeanDirectlyFailes()
   {
      assert false;
   }

   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsBindingTypes()
   {
      assert false;
   }

   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingBeanInheritsNameIfAny()
   {
      assert false;
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalInterfacesOfSpecializedFails()
   {
      assert false;
   }

   @Test(expectedExceptions=DefinitionException.class, groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testSpecializingNotSupportingLocalViewOfSpecializedFails()
   {
      assert false;
   }

   @Test(groups={"specialization", "enterpriseBeans"})
   @SpecAssertion(section = "3.3.6")
   public void testXMLDefinedSpecializationOnAnnotationDefinedBean()
   {
      assert false;
   }
   
   @Test
   @SpecAssertion(section = "3.3.7")
   public void testDefaultName()
   {
      EnterpriseBeanModel<Pitbull> pitbull = Util.createEnterpriseBeanModel(Pitbull.class, manager);
      assert pitbull.getName().equals("pitbull");
   }

   @Test(expectedExceptions=DefinitionException.class)
   public void testStatelessEnterpriseBeansWithDestructorAnnotationFails() 
   {
      EnterpriseBeanModel<WelshCorgie> welshCorgie = Util.createEnterpriseBeanModel(WelshCorgie.class, manager);
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
