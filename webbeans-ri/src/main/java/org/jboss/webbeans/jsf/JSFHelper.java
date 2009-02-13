package org.jboss.webbeans.jsf;

import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

public class JSFHelper
{
   private static final String CONVERSATION_PROPAGATION_COMPONENT_ID = "webbeans_conversation_propagation";
   private static final String CONVERSATION_ID_NAME = "cid";

   public static boolean isPostback()
   {
      return FacesContext.getCurrentInstance().getRenderKit().getResponseStateManager().isPostback(FacesContext.getCurrentInstance());
   }

   public static void removePropagationComponent()
   {
      HtmlInputHidden propagationComponent = getPropagationComponent();
      if (propagationComponent != null)
      {
         FacesContext.getCurrentInstance().getViewRoot().getChildren().remove(propagationComponent);
      }
   }

   public static void createOrUpdatePropagationComponent(String cid)
   {
      HtmlInputHidden propagationComponent = getPropagationComponent();
      if (propagationComponent == null)
      {
         propagationComponent = (HtmlInputHidden) FacesContext.getCurrentInstance().getApplication().createComponent(HtmlInputHidden.COMPONENT_TYPE);
         propagationComponent.setId(CONVERSATION_PROPAGATION_COMPONENT_ID);
         FacesContext.getCurrentInstance().getViewRoot().getChildren().add(propagationComponent);
      }
      propagationComponent.setValue(cid);
   }

   private static HtmlInputHidden getPropagationComponent()
   {
      return (HtmlInputHidden) FacesContext.getCurrentInstance().getViewRoot().findComponent(CONVERSATION_PROPAGATION_COMPONENT_ID);
   }

   private static String getConversationIdFromRequest()
   {
      return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(CONVERSATION_ID_NAME);
   }

   public static String getConversationIdFromPropagationComponent()
   {
      String cid = null;
      HtmlInputHidden propagationComponent = getPropagationComponent();
      if (propagationComponent != null)
      {
         cid = propagationComponent.getValue().toString();
      }
      return cid;
   }

   public static String getConversationId()
   {
      if (isPostback())
      {
         return getConversationIdFromPropagationComponent();
      }
      else
      {
         return getConversationIdFromRequest();
      }
   }

}
