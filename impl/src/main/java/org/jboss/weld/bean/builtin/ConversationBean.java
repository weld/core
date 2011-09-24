package org.jboss.weld.bean.builtin;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.conversation.ConversationImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public class ConversationBean extends AbstractBuiltInBean<Conversation> {

    private static final Set<Type> TYPES = Arrays2.<Type>asSet(Conversation.class, Object.class);

    private Instance<ConversationContext> conversationContexts;

    public ConversationBean(BeanManagerImpl beanManager) {
        super(Conversation.class.getName(), beanManager);
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        super.initialize(environment);
        this.conversationContexts = getBeanManager().instance().select(ConversationContext.class);
    }

    public Set<Type> getTypes() {
        return TYPES;
    }

    public Conversation create(CreationalContext<Conversation> creationalContext) {
        for (ConversationContext conversationContext : getBeanManager().instance().select(ConversationContext.class)) {
            if (conversationContext.isActive()) {
                return conversationContext.getCurrentConversation();
            }
        }
        /*
        * Can't get a "real" Conversation, but we need to return something, so
        * return this dummy Conversation which will simply throw a
        * ContextNotActiveException for every method call as the spec requires.
        */
        return new ConversationImpl(conversationContexts);
    }

    public void destroy(Conversation instance, CreationalContext<Conversation> creationalContext) {

    }

    @Override
    public Class<Conversation> getType() {
        return Conversation.class;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    @Override
    public String getName() {
        return Conversation.class.getName().toLowerCase();
    }

}
