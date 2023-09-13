package org.jboss.weld.tests.contexts.conversation.weld1262;

import java.io.IOException;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.inject.Model;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

@Model
public class Crossroad {

    @Inject
    Conversation conversation;

    @Inject
    Guide guide;

    public Guide getGuide() {
        return guide;
    }

    public void startGuide() {
        conversation.begin();
        guide.setMessage("Guide is active");
    }

    public void loosingTheGuide() throws IOException {
        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + "/road.jsf");

    }

    public void goingWithGuide() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        NavigationHandler navHandler = facesContext.getApplication().getNavigationHandler();
        navHandler.handleNavigation(facesContext, null, "/road.jsf");
    }

    public void stopGuide() {
        conversation.end();
    }

}
