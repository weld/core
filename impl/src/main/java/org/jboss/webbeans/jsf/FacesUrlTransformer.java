package org.jboss.webbeans.jsf;

import javax.enterprise.inject.AnnotationLiteral;
import javax.faces.context.FacesContext;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.conversation.ConversationIdName;

/**
 * Helper class for preparing JSF URLs which include the conversation id.
 * 
 * TODO This class has the potential to be better designed to make it fit more use cases.
 * 
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
public class FacesUrlTransformer
{
   private static final String HTTP_PROTOCOL_URL_PREFIX = "http://";
   private static final String HTTPS_PROTOCOL_URL_PREFIX = "https://";
   private static final String QUERY_STRING_DELIMITER = "?";
   private static final String PARAMETER_PAIR_DELIMITER = "&";
   private static final String PARAMETER_ASSIGNMENT_OPERATOR = "=";
   
   private String url;
   private FacesContext context;
   
   public FacesUrlTransformer(String url)
   {
      this.url = url;
   }

   public FacesUrlTransformer appendConversationIdIfNecessary(String cid)
   {
      String cidParamName = CurrentManager.rootManager().getInstanceByType(String.class, new AnnotationLiteral<ConversationIdName>(){});
      int queryStringIndex = url.indexOf(QUERY_STRING_DELIMITER);
      // if there is no query string or there is a query string but the cid param is absent, then append it
      if (queryStringIndex < 0 || url.indexOf(cidParamName + PARAMETER_ASSIGNMENT_OPERATOR, queryStringIndex) < 0)
      {
         url = new StringBuilder(url).append(queryStringIndex < 0 ? QUERY_STRING_DELIMITER : PARAMETER_PAIR_DELIMITER)
            .append(cidParamName).append(PARAMETER_ASSIGNMENT_OPERATOR).append(cid).toString();
      }
      return this;
   }
   
   public String getUrl()
   {
      return url;
   }

   public FacesUrlTransformer toRedirectViewId()
   {
      if (isUrlAbsolute())
      {
         String requestPath = context().getExternalContext().getRequestContextPath();
         url = url.substring(url.indexOf(requestPath) + requestPath.length());
      } 
      else 
      {
         int lastSlash = url.lastIndexOf("/");
         if (lastSlash > 0) 
         {
            url = url.substring(lastSlash);
         }
      }
      return this;
   }

   public FacesUrlTransformer toActionUrl()
   {
      url = context().getApplication().getViewHandler().getActionURL(context(), url);
      return this;
   }

   public String encode()
   {
      return context().getExternalContext().encodeActionURL(url);
   }
   
   private FacesContext context()
   {
      if (context == null)
      {
         context = FacesContext.getCurrentInstance();
      }
      
      return context;
   }
   
   private boolean isUrlAbsolute()
   {
      // TODO: any API call to do this?
      return url.startsWith(HTTP_PROTOCOL_URL_PREFIX) || url.startsWith(HTTPS_PROTOCOL_URL_PREFIX);
   }
}