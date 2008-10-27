package org.jboss.webbeans.contexts;

import javax.webbeans.ConversationScoped;


public class ConversationContext extends NormalContext {

   public ConversationContext()
   {
      super(ConversationScoped.class);
   }

}
