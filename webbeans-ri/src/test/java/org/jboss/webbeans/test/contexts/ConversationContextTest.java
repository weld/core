package org.jboss.webbeans.test.contexts;

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
public class ConversationContextTest extends AbstractTest
{
   /**
    * For a JSF faces request, the context is active from the beginning of the
    * apply request values phase, until the response is complete.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testContextActiveFromBeginningOfApplyRequestValuesPhasetoResponseCompleteForJsfRequest()
   {
      assert false;
   }

   /**
    * For a JSF non-faces request, the context is active during the render
    * response phase
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testContextActiveDuringRenderResponsePhaseForNonFacesJsfRequest()
   {
      assert false;
   }

   /**
    * Any JSF request has exactly one associated conversation
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testJsfRequestHasExactlyOneAssociatedConversation()
   {
      assert false;
   }

   /**
    * The conversation associated with a JSF request is determined at the end of
    * the restore view phase and does not change during the request
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testAssociatedConversationOfJsfRequestIsDeterminedAtEndOfRestoreViewPhase()
   {
      assert false;
   }

   /**
    * The conversation associated with a JSF request is determined at the end of
    * the restore view phase and does not change during the request
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testAssociatedConversationOfJsfRequestDoesNotChangeDuringRequest()
   {
      assert false;
   }

   /**
    * By default, a conversation is transient
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testDefaultConversationIsTransient()
   {
      assert false;
   }

   /**
    * All long-running conversations have a string-valued unique identifier
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testConversationsHaveUniqueStringIdentifiers()
   {
      assert false;
   }

   /**
    * If the conversation associated with the current JSF request is in the
    * transient state at the end of a JSF request, it is destroyed, and the
    * conversation context is also destroyed.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testTransientConversationIsDestroyedAtEndOfJsfRequest()
   {
      assert false;
   }

   /**
    * If the conversation associated with the current JSF request is in the
    * transient state at the end of a JSF request, it is destroyed, and the
    * conversation context is also destroyed.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testTransientConversationContextIsDestroyedAtEndOfJsfRequest()
   {
      assert false;
   }

   /**
    * If the conversation associated with the current JSF request is in the
    * long-running state at the end of a JSF request, it is not destroyed
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testLongRunningConversationNotDestroyedAtEndOfJsfRequest()
   {
      assert false;
   }

   /**
    * The long-running conversation context associated with a request that
    * renders a JSF view is automatically propagated to any faces request (JSF
    * form submission) that originates from that rendered page.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testLongRunningConversationOfJsfRenderingRequestIsPropagatedToRequestFromRenderedPage()
   {
      assert false;
   }

   /**
    * The long-running conversation context associated with a request that
    * results in a JSF redirect (via a navigation rule) is automatically
    * propagated to the resulting non-faces request, and to any other subsequent
    * request to the same URL. This is accomplished via use of a GET request
    * parameter named cid containing the unique identifier of the conversation.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testLongRunningConversationOfJsfRedirectIsPropagatedToNonFacesRequest()
   {
      assert false;
   }

   /**
    * The long-running conversation associated with a request may be propagated
    * to any non-faces request via use of a GET request parameter named cid
    * containing the unique identifier of the conversation. In this case, the
    * application must manage this request parameter
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testLongRunningConversationManuallyPropagatedToNonFacesRequest()
   {
      assert false;
   }

   /**
    * When no conversation is propagated to a JSF request, the request is
    * associated with a new transient conversation.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testNewTransientRequestIsCreatedWhenNoConversationIsPropagated()
   {
      assert false;
   }

   /**
    * All long-running conversations are scoped to a particular HTTP servlet
    * session and may not cross session boundaries
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testLongRunningConversationsMayNotCrossHttpSessions()
   {
      assert false;
   }

   /**
    * When the HTTP servlet session is invalidated, all long-running
    * conversation contexts created during the current session are destroyed
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testAllLongRunningConversationContextsOfInvalidatedHttpSessionAreDestroyed()
   {
      assert false;
   }

   /**
    * The Web Bean manager is permitted to arbitrarily destroy any long-running
    * conversation that is associated with no current JSF request, in order to
    * conserve resources
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testManagerCanDestroyOrphanedLongRunningConversations()
   {
      assert false;
   }

   /**
    * If the propagated conversation cannot be restored, the request is
    * associated with a new transient conversation
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testNewTransientConversationIsCreatedWhenConversationCannotBeRestored()
   {
      assert false;
   }

   /**
    * The Web Bean manager ensures that a long-running conversation may be
    * associated with at most one request at a time, by blocking or rejecting
    * concurrent requests.
    */
   @Test(groups = { "stub", "contexts" })
   @SpecAssertion(section = "9.6.4")
   public void testConcurrentRequestsToLongRunningConversationsAreHandled()
   {
      assert false;
   }
}