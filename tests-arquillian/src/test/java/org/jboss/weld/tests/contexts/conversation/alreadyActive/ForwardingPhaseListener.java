/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.contexts.conversation.alreadyActive;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class ForwardingPhaseListener implements PhaseListener {
    private static final long serialVersionUID = 7215399908914547315L;

    public void afterPhase(PhaseEvent event) {
        if (PhaseId.RESTORE_VIEW.equals(event.getPhaseId())) {
            if ("/conversations.xhtml".equals(getViewId(event))) {
                try {
                    HttpServletRequest request = (HttpServletRequest) event.getFacesContext().getExternalContext()
                            .getRequest();
                    HttpServletResponse response = (HttpServletResponse) event.getFacesContext().getExternalContext()
                            .getResponse();
                    request.getRequestDispatcher("/result.jsf").forward(request, response);
                    event.getFacesContext().responseComplete();
                } catch (Exception e) {
                    throw new RuntimeException("blah", e);
                }
            }
        }
    }

    private String getViewId(PhaseEvent event) {
        UIViewRoot viewRoot = event.getFacesContext().getViewRoot();
        return viewRoot == null ? null : viewRoot.getViewId();
    }

    public void beforePhase(PhaseEvent event) {
        if (PhaseId.RESTORE_VIEW.equals(event.getPhaseId())) {
            String uri = ((HttpServletRequest) event.getFacesContext().getExternalContext().getRequest()).getRequestURI();
            if (uri.contains("missing-page-error")) {
                try {
                    HttpServletResponse response = (HttpServletResponse) event.getFacesContext().getExternalContext()
                            .getResponse();
                    response.sendError(404);
                    event.getFacesContext().responseComplete();
                } catch (Exception e) {
                    throw new RuntimeException("blah", e);
                }
            }
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
