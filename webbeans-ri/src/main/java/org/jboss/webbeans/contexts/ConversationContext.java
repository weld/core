package org.jboss.webbeans.contexts;

import javax.webbeans.ConversationScoped;

/**
 * The conversation context
 * 
 * @author Nicklas Karlsson
 */
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
