/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.shutdown.hook;

import static org.junit.Assert.assertFalse;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 * @see https://issues.jboss.org/browse/WELD-2051
 */
public abstract class ShutdownHookNotRegisteredTest {

    protected static final String DEPLOYMENT_NAME = "foo";

    @ArquillianResource
    private Deployer deployer;

    @RunAsClient
    @InSequence(1)
    @Test
    public void deploy() throws Exception {
        // Reset state
        Foo.IS_FOO_DESTROYED.set(false);
        // Start embedded undertow to collect test results
        // We need to use reflection because ShutdownHookTest is a part of the
        // test archive and testFooPing() is called in-container
        ShutdownHookNotRegisteredTest.class.getClassLoader().loadClass(Foo.UNDERTOW_TEST_SERVER_CLASS)
                .getDeclaredMethod("start").invoke(null);
        // Deploy the test archive - start SE app
        deployer.deploy(DEPLOYMENT_NAME);
    }

    @InSequence(2)
    @Test
    public void testFooPing() {
        // Initialize weld, use Foo bean instance but don't call shutdown
        initWeld().select(Foo.class).get().ping();
    }

    protected abstract WeldContainer initWeld();

    @RunAsClient
    @InSequence(3)
    @Test
    public void undeploy() throws Exception {
        try {
            // Undeploy the test archive - kill SE app subprocess
            deployer.undeploy(DEPLOYMENT_NAME);
            // Check whether shutdown hook was performed correctly
            assertFalse(Foo.IS_FOO_DESTROYED.get());
        } finally {
            // Stop embedded undertow
            ShutdownHookNotRegisteredTest.class.getClassLoader().loadClass(Foo.UNDERTOW_TEST_SERVER_CLASS)
                    .getDeclaredMethod("stop").invoke(null);
        }
    }

}
