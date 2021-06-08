/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.beans.CustomEvent;
import org.jboss.weld.environment.se.test.beans.InitObserverTestBean;
import org.jboss.weld.environment.se.test.beans.ObserverTestBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Peter Royle
 * @author Martin Kouba
 */
public class StartMainObserversTest {

    private StartMain startMain;

    @Before
    public void init() {
        startMain = new StartMain(new String[0]);
    }

    @Test
    public void testObservers() {
        InitObserverTestBean.reset();
        ObserverTestBean.reset();

        WeldContainer container = startMain.go();
        BeanManager manager = container.getBeanManager();
        manager.getEvent().select(CustomEvent.class).fire(new CustomEvent());

        assertTrue(ObserverTestBean.isBuiltInObserved());
        assertTrue(ObserverTestBean.isCustomObserved());
        assertTrue(ObserverTestBean.isInitObserved());

        assertTrue(InitObserverTestBean.isInitObserved());
    }

    @After
    public void cleanup() {
        startMain.shutdownNow();
    }
}
