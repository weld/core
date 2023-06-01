package org.jboss.weld.tests.contexts.conversation;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.NonexistentConversationException;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ExceptionWritingFilter implements Filter {
    @Inject
    private Conversation conversation;

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if ("nonExistentConversation".equals(request.getParameter("cid"))) {
            try {
                chain.doFilter(request, response);
                throw new RuntimeException("Expected exception not thrown");
                // because web.xml registers CDI Conversation Filter, we can directly catch NonexistentConversationException
                // without eager conversation init, this exception might be wrapped in sth like ServletException
            } catch (NonexistentConversationException e) {
                writeNonexistenConversationException(response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }


    private void writeNonexistenConversationException(ServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.print("NonexistentConversationException thrown properly\n");
        writer.print("Conversation.isTransient: " + conversation.isTransient());
    }
}
