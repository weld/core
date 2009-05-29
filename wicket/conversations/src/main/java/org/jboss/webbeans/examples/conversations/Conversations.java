package org.jboss.webbeans.examples.conversations;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Named;
import javax.enterprise.inject.Produces;

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