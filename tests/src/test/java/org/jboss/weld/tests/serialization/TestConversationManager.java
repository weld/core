package org.jboss.weld.tests.serialization;

import java.lang.reflect.Field;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;

import org.jboss.weld.conversation.AbstractConversationManager;
import org.jboss.weld.conversation.ConversationImpl;
import org.jboss.weld.conversation.ServletConversationManager;

@SessionScoped
public class TestConversationManager extends ServletConversationManager
{

   /**
    * 
    */
   private static final long serialVersionUID = 9081670661490776033L;
   
   public Instance<ConversationImpl> getConversationInstance() throws Exception
   {
      Field ccField = AbstractConversationManager.class.getDeclaredField("currentConversation");
      ccField.setAccessible(true);
      return (Instance<ConversationImpl>) ccField.get(this);
   }

}
