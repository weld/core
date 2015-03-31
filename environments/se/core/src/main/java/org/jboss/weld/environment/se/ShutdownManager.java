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
package org.jboss.weld.environment.se;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.literal.DestroyedLiteral;

@Vetoed
@Deprecated
public class ShutdownManager {

    private boolean hasShutdownBeenCalled = false;

    private final Bootstrap bootstrap;
    private final BeanManager beanManager;

    ShutdownManager(Bootstrap bootstrap, BeanManager beanManager) {
        this.bootstrap = bootstrap;
        this.beanManager = beanManager;
    }

    /**
     * Shutdown Weld SE gracefully.
     */
    public void shutdown() {
        synchronized (this) {

            if (!hasShutdownBeenCalled) {
                hasShutdownBeenCalled = true;
                try {
                    beanManager.fireEvent(new Object(), DestroyedLiteral.APPLICATION);
                } finally {
                    bootstrap.shutdown();
                }
            } else {
                WeldSELogger.LOG.debug("Skipping spurious call to shutdown");
                WeldSELogger.LOG.tracev("Spurious call to shutdown from: {0}", (Object[]) Thread.currentThread().getStackTrace());
            }
        }
    }

}
