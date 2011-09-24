/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.exceptionInPhaseListener;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class ConversationTestPhaseListener implements PhaseListener {

    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE) && event.getFacesContext().getViewRoot().getViewId().equals("/thunderstorm.jspx")) {
            throw new RuntimeException();
        }
    }

    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE) && event.getFacesContext().getViewRoot().getViewId().equals("/thunderstorm.jspx")) {
            throw new RuntimeException();
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
