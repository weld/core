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

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;

public class ShutdownManager {

    private static Logger log = Logger.getLogger(ShutdownManager.class);

    private boolean hasShutdownBeenCalled = false;

    private final Bootstrap bootstrap;

    ShutdownManager(Bootstrap bootstrap) {
        super();
        this.bootstrap = bootstrap;
    }

    /**
     * Shutdown Weld SE gracefully.
     */
    public void shutdown() {
        synchronized (this) {

            if (!hasShutdownBeenCalled) {
                hasShutdownBeenCalled = true;
                bootstrap.shutdown();
            } else {
                log.debug("Skipping spurious call to shutdown");
                log.tracev("Spurious call to shutdown from: {0}", Thread.currentThread().getStackTrace());
            }
        }
    }

}
