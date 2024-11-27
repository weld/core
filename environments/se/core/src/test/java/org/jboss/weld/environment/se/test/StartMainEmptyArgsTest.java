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

import static org.junit.Assert.assertNotNull;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.beans.MainTestBean;
import org.jboss.weld.environment.se.test.beans.ParametersTestBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Peter Royle
 */
public class StartMainEmptyArgsTest {

    private StartMain startMain;

    @Before
    public void init() {
        startMain = new StartMain(new String[0]);
    }

    /**
     * Test of main method, of class StartMain when no command-line args are
     * provided.
     */
    @Test
    public void testMainEmptyArgs() {
        WeldContainer container = startMain.go();

        MainTestBean mainTestBean = container.select(MainTestBean.class).get();
        assertNotNull(mainTestBean);

        ParametersTestBean paramsBean = mainTestBean.getParametersTestBean();
        assertNotNull(paramsBean);
        assertNotNull(paramsBean.getParameters());
    }

    @After
    public void cleanup() {
        startMain.shutdownNow();
    }
}
