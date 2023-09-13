/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.jsf;

import java.util.List;
import java.util.Map;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.context.FacesContext;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;

/**
 * <p>
 * A forwarding JSF ViewHandler implementation that produces URLs containing the
 * conversation id query string parameter. All methods except those which
 * produce a URL that need to be enhanced are forwarded to the ViewHandler
 * delegate.
 * </p>
 * <p/>
 * <p>
 * A request parameter was chosen to propagate the conversation because it's
 * the most technology agnostic approach for passing data between requests and
 * allows for the ensuing request to use whatever means necessary (a servlet
 * filter, phase listener, etc) to capture the conversation id and restore the
 * long-running conversation.
 * </p>
 *
 * @author Dan Allen
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class ConversationAwareViewHandler extends ViewHandlerWrapper {

    private static enum Source {

        ACTION,
        BOOKMARKABLE,
        REDIRECT,
        RESOURCE

    }

    private final ViewHandler delegate;
    private volatile ConversationContext conversationContext;
    private static final ThreadLocal<Source> source = new ThreadLocal<Source>();
    private String contextId;

    public ConversationAwareViewHandler(ViewHandler delegate) {
        this.delegate = delegate;
    }

    /**
     * Get conversation context. May return null if the container is not available.
     *
     * @return the conversation context or null if the container is not booted
     */
    private ConversationContext getConversationContext(String id) {
        if (conversationContext == null) {
            synchronized (this) {
                if (conversationContext == null) {
                    if (!Container.available(id)) {
                        return null;
                    }
                    Container container = Container.instance(id);
                    conversationContext = container.deploymentManager().instance().select(HttpConversationContext.class).get();
                }
            }
        }
        return conversationContext;
    }

    /**
     * Allow the delegate to produce the action URL. If the conversation is
     * long-running, append the conversation id request parameter to the query
     * string part of the URL, but only if the request parameter is not already
     * present.
     * <p/>
     * This covers form actions Ajax calls, and redirect URLs (which we want) and
     * link hrefs (which we don't)
     *
     * @see {@link ViewHandler#getActionURL(FacesContext, String)}
     */
    @Override
    public String getActionURL(FacesContext facesContext, String viewId) {
        if (contextId == null) {
            if (facesContext.getAttributes().containsKey(Container.CONTEXT_ID_KEY)) {
                contextId = (String) facesContext.getAttributes().get(Container.CONTEXT_ID_KEY);
            } else {
                contextId = RegistrySingletonProvider.STATIC_INSTANCE;
            }
        }
        String actionUrl = super.getActionURL(facesContext, viewId);
        final ConversationContext ctx = getConversationContext(contextId);
        if (ctx != null && ctx.isActive() && !getSource().equals(Source.BOOKMARKABLE)
                && !ctx.getCurrentConversation().isTransient()) {
            return new FacesUrlTransformer(actionUrl, facesContext)
                    .appendConversationIdIfNecessary(getConversationContext(contextId).getParameterName(),
                            ctx.getCurrentConversation().getId())
                    .getUrl();
        } else {
            return actionUrl;
        }
    }

    private Source getSource() {
        if (source.get() == null) {
            return Source.ACTION;
        } else {
            return source.get();
        }
    }

    @Override
    public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters,
            boolean includeViewParams) {
        try {
            source.set(Source.BOOKMARKABLE);
            return super.getBookmarkableURL(context, viewId, parameters, includeViewParams);
        } finally {
            source.remove();
        }
    }

    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters,
            boolean includeViewParams) {
        try {
            source.set(Source.REDIRECT);
            return super.getRedirectURL(context, viewId, parameters, includeViewParams);
        } finally {
            source.remove();
        }
    }

    @Override
    public String getResourceURL(FacesContext context, String path) {
        try {
            source.set(Source.RESOURCE);
            return super.getResourceURL(context, path);
        } finally {
            source.remove();
        }
    }

    @Override
    public ViewHandler getWrapped() {
        return delegate;
    }

}
