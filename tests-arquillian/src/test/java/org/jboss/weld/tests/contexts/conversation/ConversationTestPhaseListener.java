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
package org.jboss.weld.tests.contexts.conversation;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;

public class ConversationTestPhaseListener implements PhaseListener {

    public static final String CID_REQUEST_PARAMETER_NAME = "cid";

    public static final String CID_HEADER_NAME = "org.jboss.jsr299.tck.cid";

    public static final String LONG_RUNNING_HEADER_NAME = "org.jboss.jsr299.tck.longRunning";

    /**
     *
     */
    private static final long serialVersionUID = 1197355854770726526L;

    public static final String ACTIVE_BEFORE_APPLY_REQUEST_VALUES_HEADER_NAME = "org.jboss.jsr299.tck.activeBeforeApplyRequestValues";

    private boolean activeBeforeApplyRequestValues;

    public void afterPhase(PhaseEvent event) {
    }

    public void beforePhase(PhaseEvent event) {
        BeanManagerImpl beanManager;
        try {
            beanManager = BeanManagerProxy.unwrap((BeanManager) new InitialContext().lookup("java:comp/BeanManager"));
        } catch (NamingException e) {
            throw new RuntimeException("Error looking up java:comp/BeanManager ", e);
        }
        if (event.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES)) {
            try {
                beanManager.getContext(ConversationScoped.class);
                activeBeforeApplyRequestValues = true;
            } catch (ContextNotActiveException e) {
                activeBeforeApplyRequestValues = false;
            }
        }
        if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
            Conversation conversation = beanManager.instance().select(Conversation.class).get();
            HttpServletResponse response = (HttpServletResponse) event.getFacesContext().getExternalContext().getResponse();
            response.addHeader(CID_HEADER_NAME, conversation.getId() == null ? " null" : conversation.getId());
            response.addHeader(LONG_RUNNING_HEADER_NAME, String.valueOf(!conversation.isTransient()));
            response.addHeader(Cloud.RAINED_HEADER_NAME,
                    new Boolean(beanManager.instance().select(Cloud.class).get().isRained()).toString());
            response.addHeader(ACTIVE_BEFORE_APPLY_REQUEST_VALUES_HEADER_NAME,
                    new Boolean(activeBeforeApplyRequestValues).toString());
        }
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
