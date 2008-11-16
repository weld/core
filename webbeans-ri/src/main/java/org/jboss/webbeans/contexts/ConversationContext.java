package org.jboss.webbeans.contexts;

import javax.webbeans.ConversationScoped;


public class ConversationContext extends PrivateContext {

   public ConversationContext()
   {
      super(ConversationScoped.class);
   }
   
   @Override
   public String toString()
   {
      return "Conversation context";
   }   

}
