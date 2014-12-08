/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.context.async.symmetry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

@WebListener
public class SimpleListener implements ServletRequestListener {

    static final List<Thread> THREADS = Collections.synchronizedList(new LinkedList<Thread>());

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        process((HttpServletRequest) sre.getServletRequest());
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        process((HttpServletRequest) sre.getServletRequest());
    }

    private void process(HttpServletRequest request) {
        if (request.getRequestURI().endsWith("/async")) {
            THREADS.add(Thread.currentThread());
        }
    }
}
