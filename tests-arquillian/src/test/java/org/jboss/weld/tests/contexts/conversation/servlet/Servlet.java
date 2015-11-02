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
package org.jboss.weld.tests.contexts.conversation.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/servlet/*")
public class Servlet extends HttpServlet {

    @Inject
    private Message message;

    @Inject
    private Conversation conversation;

    @Inject
    private DestroyedConversationObserver observer;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/display")) {
            printInfo(resp.getWriter());
        } else if (uri.endsWith("/redirect")) {
            resp.sendRedirect("servlet/display"); // do a redirect, the cid is not propagated;
        } else if (uri.endsWith("/begin")) {
            conversation.begin();
            message.setCid(conversation.getId());
            printInfo(resp.getWriter());
        } else if (uri.endsWith("/end")) {
            conversation.end();
            printInfo(resp.getWriter());
        } else if (uri.endsWith("/set")) {
            setMessage(req);
            printInfo(resp.getWriter());
        } else if (uri.endsWith("/invalidateSession")) {
            observer.reset();
            req.getSession().invalidate();
            printInfo(resp.getWriter());
        } else if (uri.endsWith("/listDestroyedMessages")) {
            PrintWriter writer = resp.getWriter();
            writer.append("DestroyedMessages: ");
            printMessages(writer, observer.getDestroyedMessages());
        } else if (uri.endsWith("/listConversationsDestroyedWhileBeingAssociated")) {
            PrintWriter writer = resp.getWriter();
            writer.append("ConversationsDestroyedWhileBeingAssociated: ");
            printSessionIds(writer, observer.getAssociatedConversationIds());
        } else if (uri.endsWith("/listConversationsDestroyedWhileBeingDisassociated")) {
            PrintWriter writer = resp.getWriter();
            writer.append("ConversationsDestroyedWhileBeingDisassociated: ");
            printSessionIds(writer, observer.getDisassociatedConversationIds());
        } else {
            resp.setStatus(404);
        }
        resp.setContentType("text/plain");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/set")) {
            setMessage(req);
            printInfo(resp.getWriter());
        } else {
            resp.setStatus(404);
        }
        resp.setContentType("text/plain");
    }

    private void printInfo(PrintWriter writer) {
        writer.append("message: ").append(message.getValue());
        writer.append('\n');
        writer.append("cid: [").append(conversation.getId());
        writer.append(']');
        writer.append('\n');
        writer.append("transient: ").append(Boolean.toString(conversation.isTransient()));
        writer.append('\n');
    }

    private void printSessionIds(PrintWriter writer, Set<String> ids) {
        for (String id : ids) {
            writer.append('<').append(id).append('>');
        }
    }

    private void printMessages(PrintWriter writer, List<Message> messages) {
        for (Message message : messages) {
            writer.append("<M:").append(message.getCid()).append('>');
        }
    }

    private void setMessage(HttpServletRequest request) {
        String value = request.getParameter("message");
        if (value == null) {
            throw new IllegalArgumentException("message must be specified");
        }
        message.setValue(value);
    }
}
