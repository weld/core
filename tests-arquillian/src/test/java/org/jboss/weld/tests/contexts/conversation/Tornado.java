package org.jboss.weld.tests.contexts.conversation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

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
