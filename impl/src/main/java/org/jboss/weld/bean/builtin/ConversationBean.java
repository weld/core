package org.jboss.weld.bean.builtin;

import java.lang.annotation.Annotation;
import java.util.Locale;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.contexts.conversation.ConversationImpl;
import org.jboss.weld.manager.BeanManagerImpl;

public class ConversationBean extends AbstractStaticallyDecorableBuiltInBean<Conversation> {

    public ConversationBean(BeanManagerImpl beanManager) {
        super(beanManager, Conversation.class);
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
    }

    @Override
    protected Conversation newInstance(InjectionPoint ip, CreationalContext<Conversation> creationalContext) {
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
        return new ConversationImpl(beanManager);
    }

    @Override
    public Class<?> getBeanClass() {
        return ConversationImpl.class;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    @Override
    public String getName() {
        return Conversation.class.getName().toLowerCase(Locale.ENGLISH);
    }

}
