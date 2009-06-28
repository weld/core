package org.jboss.webbeans.test.unit.context;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.testng.annotations.Test;

@Artifact
public class PassivatingContextTest extends org.jboss.webbeans.test.AbstractWebBeansTest
{
   
   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsSessionScopePassivating()
   {
      assert getCurrentManager().getServices().get(MetaAnnotationStore.class).getScopeModel(SessionScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsConversationScopePassivating()
   {
      assert getCurrentManager().getServices().get(MetaAnnotationStore.class).getScopeModel(ConversationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsApplicationScopeNonPassivating()
   {
      assert !getCurrentManager().getServices().get(MetaAnnotationStore.class).getScopeModel(ApplicationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   public void testIsRequestScopeNonPassivating()
   {
      assert !getCurrentManager().getServices().get(MetaAnnotationStore.class).getScopeModel(RequestScoped.class).isPassivating();
   }
   
}
