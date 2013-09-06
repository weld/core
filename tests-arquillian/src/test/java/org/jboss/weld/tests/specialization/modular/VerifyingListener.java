/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.specialization.modular;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class VerifyingListener implements ServletContextListener {

    @Inject
    private InjectedBean1 bean1;

    @Inject
    private InjectedBean2 bean2;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        assertTrue(bean1.getFactory().get() instanceof SpecializedFactory);
        assertTrue(bean1.getProduct().isUnsatisfied());

        assertTrue(bean2.getFactory().isUnsatisfied());
        assertTrue(bean2.getProduct().isUnsatisfied());
    }

    public void assertTrue(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // noop
    }
}
