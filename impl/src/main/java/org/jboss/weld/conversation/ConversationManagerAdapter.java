package org.jboss.weld.conversation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class ConversationManagerAdapter implements ConversationManager
{

   public ConversationManagerAdapter()
   {
   }

   @Inject
   private ConversationManager2 conversationManager;

   public void beginOrRestoreConversation(String cid)
   {
      conversationManager.setupConversation(cid);
   }

   public void cleanupConversation()
   {
      conversationManager.teardownConversation();
   }

   public void destroyAllConversations()
   {
      conversationManager.teardownContext();
   }

   public Set<Conversation> getLongRunningConversations()
   {
      Set<Conversation> conversations = new HashSet<Conversation>();
      conversations.addAll(conversationManager.getConversations().values());
      return Collections.unmodifiableSet(conversations);
   }

}
