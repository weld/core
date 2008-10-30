package org.jboss.webbeans.test;

import javax.webbeans.manager.Context;

import org.jboss.webbeans.contexts.RequestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 */
@SpecVersion("20081029-PDR")
public class PassivatingContextTest extends AbstractTest
{
   Context context;

   @BeforeMethod
   public void initContext()
   {
      context = new RequestContext();
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testIsSessionScopePassivating()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testIsConversationScopePassivating()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testIsApplicationScopeNonPassivating()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testIsRequestScopePassivating()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testEJBWebBeanCanDefinePassivatingScope()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleWebBeanWithNonSerializableImplementationClassFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testProducerMethodDeclaringPassivatingScopeButReturningNonSerializableImplementationClassFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleWebBeanDeclaringPassivatingScopeIsSerializedWhenContextIsPassivated()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testStatefulEJBIsSerializedWhenPassivatedByEJBContainer()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testReferencesToWebBeansAreSerializable()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentEJBsAreSerializable()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoStatefulSessionBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoStatefulSessionBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      assert false;
   }

   @Test(groups = { "contexts", "passivation" }) @SpecAssertion(section = "8.4")
   public void testJMSEndpointProxyIsSerializable()
   {
      assert false;
   }
}
