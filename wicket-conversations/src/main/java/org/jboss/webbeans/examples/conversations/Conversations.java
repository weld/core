package org.jboss.webbeans.examples.conversations;

import java.io.Serializable;

import javax.annotation.Named;
import javax.context.SessionScoped;
import javax.inject.Produces;

import org.jboss.webbeans.conversation.ConversationInactivityTimeout;


@SessionScoped
@Named("conversations")
public class Conversations implements Serializable {

   @Produces
   @ConversationInactivityTimeout
   @Example
   public static long getConversationTimeoutInMilliseconds()
   {
      return 600000;
   }   
}