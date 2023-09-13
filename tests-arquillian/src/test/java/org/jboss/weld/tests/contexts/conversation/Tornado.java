package org.jboss.weld.tests.contexts.conversation;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ConversationScoped
@Named
public class Tornado implements Serializable {

    @Inject
    Conversation conversation;

    private String name;

    @PostConstruct
    void init() {
        name = "Pete";
    }

    public String beginConversation() {
        conversation.begin();
        name = "Shane";
        return "conversationBegun";
    }

    public String endAndBeginConversation() {
        conversation.end();
        conversation.begin();
        return "conversationEndedAndBegun";
    }

    public String getName() {
        return name;
    }

}
