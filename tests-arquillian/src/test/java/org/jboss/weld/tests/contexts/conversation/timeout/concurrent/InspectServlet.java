/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.timeout.concurrent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.test.util.Timer;

@WebServlet(value = "/inspect", urlPatterns = "/*")
public class InspectServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(InspectServlet.class.getName());

    public static final String MODE_INIT = "init";
    public static final String MODE_LONG_TASK = "long";
    public static final String MODE_BUSY_REQUEST = "busy";

    @Inject
    Conversation conversation;

    @Inject
    StateHolder stateHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/plain");
        String mode = req.getParameter("mode");

        if (MODE_INIT.equals(mode)) {
            conversation.begin();
            stateHolder.reset();
            resp.getWriter().write(conversation.getId() + "::" + req.getSession().getId());
        } else if (MODE_LONG_TASK.equals(mode)) {
            if (conversation.isTransient()) {
                resp.sendError(500, "No long running conversation");
            } else {
                long start = System.currentTimeMillis();
                try {
                    new Timer().setSleepInterval(100l).setDelay(2, TimeUnit.SECONDS)
                            .addStopCondition(new Timer.StopCondition() {
                                @Override
                                public boolean isSatisfied() {
                                    return stateHolder.isBusyAttemptMade();
                                }
                            }).start();
                } catch (InterruptedException e1) {
                    throw new IllegalStateException();
                }
                logger.info("Long task finished [isBusyAttemptMade: " + stateHolder.isBusyAttemptMade() + ", time: " + String
                        .valueOf(System.currentTimeMillis() - start));
                resp.getWriter().write("OK");
            }
        } else if (MODE_BUSY_REQUEST.equals(mode)) {
            resp.getWriter().write("Conversation locked");
        } else {
            throw new ServletException("Unknown test mode");
        }
    }
}
