package org.jboss.weld.tests.contexts.errorpage;

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ConversationScoped
public class Storm implements Serializable
{
   
   /**
	 * 
	 */
	private static final long serialVersionUID = -1513633490356967202L;

	@Inject Conversation conversation;
   
   private String strength = "0";
   
   public void beginConversation()
   {
      conversation.begin();
   }
   
   public void disaster()
   {
      throw new RuntimeException("Storm is a disaster");
   }

   public String getStrength()
   {
      return strength;
   }
   
   public void setStrength(String strength)
   {
      this.strength = strength;
   }
   
}
