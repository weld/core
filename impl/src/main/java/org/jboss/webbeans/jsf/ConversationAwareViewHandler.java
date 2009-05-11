package org.jboss.webbeans.jsf;

import javax.context.Conversation;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.inject.manager.Manager;

import org.jboss.webbeans.CurrentManager;

/**
 * <p>
 * A forwarding JSF ViewHandler implementation that produces URLs containing the
 * conversation id query string parameter. All methods except those which
 * produce a URL that need to be enhanced are forwarded to the ViewHandler
 * delegate.
 * </p>
 * 
 * <p>
 * A request parameter was choosen to propagate the conversation because it's
 * the most technology agnostic approach for passing data between requests and
 * allows for the ensuing request to use whatever means necessary (a servlet
 * filter, phase listener, etc) to capture the conversation id and restore the
 * long-running conversation.
 * </p>
 * QUESTION should we do the same for getResourceURL?
 * TODO we should enable a way to disable conversation propagation by URL
 * 
 * @author Dan Allen
 */
public class ConversationAwareViewHandler extends ViewHandlerWrapper
{
   private ViewHandler delegate;

   public ConversationAwareViewHandler(ViewHandler delegate)
   {
      this.delegate = delegate;
   }

   /**
    * Allow the delegate to produce the action URL. If the conversation is
    * long-running, append the conversation id request parameter to the query
    * string part of the URL, but only if the request parameter is not already
    * present.
    *
    * This covers all cases: form actions, link hrefs, Ajax calls, and redirect URLs. 
    * 
    * @see {@link ViewHandler#getActionURL(FacesContext, String)}
    */
   @Override
   public String getActionURL(FacesContext context, String viewId)
   {
      String actionUrl = super.getActionURL(context, viewId);
      Manager manager = CurrentManager.rootManager();
      Conversation conversation = manager.getInstanceByType(Conversation.class);
      if (conversation.isLongRunning())
      {
         return new FacesUrlTransformer(actionUrl).appendConversationIdIfNecessary(conversation.getId()).getUrl();
      }
      else
      {
         return actionUrl;
      }
   }

   /**
    * @see {@link ViewHandlerWrapper#getWrapped()}
    */
   @Override
   public ViewHandler getWrapped()
   {
      return delegate;
   }

}
