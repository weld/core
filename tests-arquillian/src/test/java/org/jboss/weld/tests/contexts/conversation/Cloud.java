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

import java.io.Serializable;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ConversationScoped
public class Cloud implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5765109971012677278L;

    public static final String NAME = Cloud.class.getName() + ".Pete";

    public static final String RAINED_HEADER_NAME = Cloud.class.getName() + ".rained";

    public static final String CUMULUS = "cumulus";

    private boolean rained;

    private String name = NAME;

    @Inject
    Conversation conversation;

    @Inject
    Hurricane hurricane;

    @PreDestroy
    public void destroy() {
        hurricane.setPreDestroyCalledOnCloud(true);
    }

    public String getName() {
        return name;
    }

    public void rain() {
        rained = true;
        System.out.println("rain!");
    }

    public boolean isRained() {
        return rained;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String thunderstorm() {
        conversation.begin();
        return "thunder";
    }

    public String hailstorm() {
        conversation.begin();
        return "hail";
    }

    public String hurricane() {
        conversation.begin();
        return "wind";
    }

    public String snowstorm() {
        conversation.begin();
        return "snow";
    }

    public String invalidateSession() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "sessionInvalidated";
    }

    public String sleet() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "sleet";
    }

    public String blizzard() {
        this.name = "henry";
        conversation.begin();
        return "blizzard";
    }

    public String cumulus() {
        this.name = CUMULUS;
        conversation.begin();
        return "cumulus";
    }
}
