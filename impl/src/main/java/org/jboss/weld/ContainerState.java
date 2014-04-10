/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld;

/**
 * Application container instance state
 *
 * @author pmuir
 */
public enum ContainerState {
    /**
     * The container has not been started
     */
    STOPPED(false),
    /**
     * The container is starting
     */
    STARTING(false),
    /**
     * The container has finished bean discovery. Beans are not fully initialized yet.
     */
    DISCOVERED(false),
    /**
     * The container has started and beans have been deployed
     */
    DEPLOYED(true),
    /**
     * The deployment has been validated
     */
    VALIDATED(true),
    /**
     * The container finished initialization and is serving requests
     */
    INITIALIZED(true),
    /**
     * The container has been shutdown
     */
    SHUTDOWN(false);

    private ContainerState(boolean available) {
        this.available = available;
    }

    final boolean available;

    /**
     * Whether the container is available for use
     *
     * @return
     */
    public boolean isAvailable() {
        return available;
    }
}
