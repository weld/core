package org.jboss.weld.tests.contexts.conversation.weld1262;

import java.io.Serializable;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Named;

@ConversationScoped
@Named
public class Guide implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String message = "Guide is not active";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
