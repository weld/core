package org.jboss.webbeans.test.unit.context;

import javax.context.ApplicationScoped;
import javax.context.ConversationScoped;
import javax.context.RequestScoped;
import javax.context.SessionScoped;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.testng.annotations.Test;

@Artifact
public class PassivatingContextTest extends org.jboss.webbeans.test.unit.AbstractWebBeansTest
{
   
   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsSessionScopePassivating()
   {
      assert MetaDataCache.instance().getScopeModel(SessionScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsConversationScopePassivating()
   {
      assert MetaDataCache.instance().getScopeModel(ConversationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsApplicationScopeNonPassivating()
   {
      assert !MetaDataCache.instance().getScopeModel(ApplicationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsRequestScopeNonPassivating()
   {
      assert !MetaDataCache.instance().getScopeModel(RequestScoped.class).isPassivating();
   }
   
}
