package org.jboss.webbeans.examples.conversations;

import java.util.ArrayList;
import java.util.List;

import javax.context.Conversation;
import javax.inject.Current;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jboss.webbeans.conversation.ConversationManager;

/**
 * This is a port of the JSF-based "conversation" example to use Wicket and the
 * Wicket/WebBeans integration module. This shows how long-running conversations
 * are propogated across redirects using wicket page metadata (i.e. without
 * url-rewriting), and how wicket components can use WebBeans injections.
 * 
 * @author cpopetz
 * 
 */
public class HomePage extends WebPage
{

   private static final long serialVersionUID = 1L;

   /**
    * These are injections for displaying information about existing conversations to the user
    */
   @Current ConversationManager conversationManager;
   @Current Conversation currentConversation;
   
   /**
    * This is our conversational data component, to illustrate how data is used in conversations and
    * exists per-conversation.
    */
   @Current Data data;
   
   /**
    * This is the conversation we'll switch to, chosen by the popup 
    */
   transient Conversation chosenConversation;

   @SuppressWarnings("serial")
   public HomePage()
   {

      chosenConversation = currentConversation;
      
      add(new FeedbackPanel("feedback"));
      
      //the first from is to change the value in the conversational data component
      Form form = new Form("form");
      add(form);
      form.add(new TextField("dataField", new PropertyModel(this, "data.data")));
      form.add(new Button("changeValue"));


      //the second form is for switching conversations
      form = new Form("conversationForm"); 
      add(form);
      form.add(new DropDownChoice("cidSelect",
            new PropertyModel(this,"chosenConversation"),
            new Model() { 
		         public Object getObject()
		         {
		            //the popup is the set of all long running conversations, plus our own conversation if it is not long running
		            List<Conversation> list = 
		               new ArrayList<Conversation>(conversationManager.getLongRunningConversations());
		            if (!currentConversation.isLongRunning())
		            {
		               list.add(0, currentConversation);
		            }
		            return list;
		         }
		      }) { 
         protected boolean wantOnSelectionChangedNotifications() { return true; }
         protected void onSelectionChanged(Object newSelection)
         {
            getSession().info("Switched to conversation " + chosenConversation.getId());
            setRedirect(true);
            setResponsePage(HomePage.class,new PageParameters("cid=" + chosenConversation.getId()));
         }      });

      form = new Form("buttons");
      
      add(form);
      form.add(new Button("begin")
      {
         public void onSubmit()
         {
            currentConversation.begin();
            getSession().info("conversation " + currentConversation.getId() + " promoted to long-running.");
            //we do this because the conversationManager doesn't update its list
            //until we cleanup the conversation after the request, so we want a redirect
            //in order to get the select to look right
            setRedirect(true);
            setResponsePage(HomePage.class);
         }
      });

      form.add(new Button("noop"));

      form.add(new Button("end")
      {
         public void onSubmit()
         {
            getSession().info("Ended conversation " + currentConversation.getId());
            currentConversation.end();
            //we do this because the conversationManager doesn't update its list
            //until we cleanup the conversation after the request, so we want a redirect
            //in order to get the select to look right
            setRedirect(true);
            setResponsePage(HomePage.class);
         }
      });

      form.add(new Button("longop")
      {
         public void onSubmit()
         {
            try
            {
               Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
            }
         }
      });

   }
}
