package org.jboss.webbeans.test.contexts;

import javax.webbeans.ContextNotActiveException;

import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 */
@SpecVersion("20081206")
public class ContextManagement extends AbstractTest
{
   /**
    * For each of the built-in normal scopes, contexts propagate across any Java
    * method call, including invocation of EJB local business methods.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6")
   public void testBuiltInNormalScopedContextsPropagateAcrossAnyJavaMethodCall()
   {
      assert false;
   }

   /**
    * The built-in contexts do not propagate across remote method invocations or
    * to asynchronous processes such as JMS message listeners or EJB timer
    * service timeouts
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6")
   public void testBuiltInNormalScopedContextsDoNotPropagateAcrossRemoteMethodInvocations()
   {
      assert false;
   }

   /**
    * The built-in contexts do not propagate across remote method invocations or
    * to asynchronous processes such as JMS message listeners or EJB timer
    * service timeouts
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6")
   public void testBuiltInNormalScopedContextsDoNotPropagateAcrossAsynchronousMethodInvocations()
   {
      assert false;
   }

   /**
    * If no active context object exists for the given scope type, getContext()
    * must throw a ContextNotActiveException.
    */
   @Test(groups = { "stub", "contexts" }, expectedExceptions = ContextNotActiveException.class)
   @SpecAssertion(section = "9.7")
   public void testGettingContextNotActiveFails()
   {
      assert false;
   }

   /**
    * If more than one active context object exists for the given scope type,
    * getContext() must throw an IllegalStateException.
    */
   @Test(groups = { "stub", "contexts" }, expectedExceptions = ContextNotActiveException.class)
   @SpecAssertion(section = "9.7")
   public void testGettingContextWithTooManyActiveFails()
   {
      assert false;
   }

}