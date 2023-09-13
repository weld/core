/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.protocol.jmx.JMXMethodExecutor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test, while it passes, logs a series of expected errors, as Observer calls System.exit(). In order to prevent this, we
 * SUSPEND logging for a short period of time right after first method, then we re-enable it in the beginning of the third.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ShutdownHookRegisteredBeforeInitEventTest extends AbstractShutdownHookTest {

    private Level originalLogLevel;

    @Deployment(managed = false, name = DEPLOYMENT_NAME)
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class).addClasses(AbstractShutdownHookTest.class,
                ShutdownHookRegisteredBeforeInitEventTest.class, Foo.class, Observer.class)).build();
    }

    @RunAsClient
    @InSequence(1)
    @Test
    public void deploy() throws Exception {
        try {
            super.deploy();
        } finally {
            // additionally, disable log for following test to prevent flooding it with expected errors
            Logger logger = Logger.getLogger(JMXMethodExecutor.class.getName());
            originalLogLevel = logger.getLevel();
            logger.setLevel(Level.OFF);
        }
    }

    @Override
    @InSequence(2)
    @Test(expected = UndeclaredThrowableException.class)
    public void testFooPing() {
        // Initialize weld, Foo bean is used in observer
        new Weld().initialize();
    }

    @RunAsClient
    @InSequence(3)
    @Test
    public void undeploy() throws Exception {
        // set the logging level back to what we had before
        Logger.getLogger(JMXMethodExecutor.class.getName()).setLevel(originalLogLevel);

        super.undeploy();
    }
}
