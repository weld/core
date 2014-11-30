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
package org.jboss.weld.experimental;

import javax.enterprise.inject.spi.EventMetadata;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link EventMetadata}.
 *
 * @author Jozef Hartinger
 * @seeIssue WELD-1793
 *
 */
public interface ExperimentalEventMetadata extends EventMetadata {

    /**
     * Indicates whether this event was fired asynchronously.
     * @return true if and only if the event was fired asynchronously
     */
    boolean isAsync();
}
