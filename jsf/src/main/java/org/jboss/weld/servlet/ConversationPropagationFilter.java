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
package org.jboss.weld.servlet;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.jsf.FacesUrlTransformer;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * <p>
 * A Filter for handling conversation propagation over redirects.
 * </p>
 * <p/>
 * <p>
 * This fiter intercepts the call to
 * {@link HttpServletResponse#sendRedirect(String)} and appends the conversation
 * id request parameter to the URL if the conversation is long-running, but only
 * if the request parameter is not already present.
 * </p>
 *
 * @author Nicklas Karlsson
 */
public class ConversationPropagationFilter implements Filter {
    private String contextId;

    public void init(FilterConfig config) throws ServletException {
        contextId = (String) config.getServletContext().getAttribute(Container.CONTEXT_ID_KEY);
        if (contextId == null) {
            contextId = RegistrySingletonProvider.STATIC_INSTANCE;
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            response = wrapResponse((HttpServletResponse) response, ((HttpServletRequest) request).getContextPath());
        }
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    private ServletResponse wrapResponse(HttpServletResponse response, final String requestPath) {
        return new HttpServletResponseWrapper(response) {
            @Override
            public void sendRedirect(String path) throws IOException {
                FacesContext context = FacesContext.getCurrentInstance();
                if (context != null) { // this is a JSF request
                    ConversationContext conversationContext = instance(contextId).select(HttpConversationContext.class).get();
                    if (conversationContext.isActive()) {
                        Conversation conversation = conversationContext.getCurrentConversation();
                        if (!conversation.isTransient()) {
                            path = new FacesUrlTransformer(path, context)
                                .toRedirectViewId()
                                .toActionUrl()
                                .appendConversationIdIfNecessary(conversationContext.getParameterName(), conversation.getId())
                                .encode();
                        }
                    }
                }
                super.sendRedirect(path);
            }
        };
    }


    private static Instance<Context> instance(String id) {
        return Container.instance(id).deploymentManager().instance().select(Context.class);
    }
}
