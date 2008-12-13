package org.jboss.webbeans.test.contexts;

import javax.webbeans.DefinitionException;
import javax.webbeans.IllegalProductException;
import javax.webbeans.UnserializableDependencyException;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 */
@SpecVersion("20081206")
public class PassivatingContextTest extends AbstractTest
{
   Context context;

   @BeforeMethod
   public void initContext()
   {
      context = new RequestContext()
      {
      };
   }

   /**
    * EJB local objects are serializable. Therefore, an enterprise Web Bean may
    * declare any passivating scope.
    */
   @Test(groups = { "stub", "contexts", "passivation", "enterpriseBean" })
   @SpecAssertion(section = "9.5")
   public void testEJBWebBeanCanDeclarePassivatingScope()
   {
      assert false;
   }

   /**
    * Simple Web Beans are not required to be serializable. If a simple Web Bean
    * declares a passivating scope, and the implementation class is not
    * serializable, a DefinitionException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleWebBeanWithNonSerializableImplementationClassFails()
   {
      assert false;
   }

   /**
    * If a producer method or field declares a passivating scope and returns a
    * non-serializable object at runtime, an Illegal- ProductException is thrown
    * by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testProducerMethodDeclaringPassivatingScopeButReturningNonSerializableImplementationClassFails()
   {
      assert false;
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsSessionScopePassivating()
   {
      assert false;
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsConversationScopePassivating()
   {
      assert false;
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsApplicationScopeNonPassivating()
   {
      assert false;
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsRequestScopeNonPassivating()
   {
      assert false;
   }

   /**
    * the Web Bean declares a passivating scope type, and context passivation
    * occurs, or
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testSimpleWebBeanDeclaringPassivatingScopeIsSerializedWhenContextIsPassivated()
   {
      assert false;
   }

   /**
    * the Web Bean is an EJB stateful session bean, and it is passivated by the
    * EJB container.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testStatefulEJBIsSerializedWhenPassivatedByEJBContainer()
   {
      assert false;
   }

   /**
    * Therefore, any reference to a Web Bean which declares a normal scope type
    * is serializable.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testReferencesToWebBeansWithNormalScopeAreSerializable()
   {
      assert false;
   }

   /**
    * EJB local objects are serializable. Therefore, any reference to an
    * enterprise Web Bean of scope @Dependent is serializable.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testDependentEJBsAreSerializable()
   {
      assert false;
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoStatefulSessionBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoStatefulSessionBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      assert false;
   }

   /**
    * The Web Bean manager must guarantee that JMS endpoint proxy objects are
    * serializable.
    */
   @Test(groups = { "stub", "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testJMSEndpointProxyIsSerializable()
   {
      assert false;
   }
}
