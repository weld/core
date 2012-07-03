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
package org.jboss.weld.logging;

import ch.qos.cal10n.IMessageConveyor;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class LoggerFactory {

    private static volatile LoggerFactory INSTANCE;

    public static void cleanup() {
        INSTANCE = null;
    }

    private final LocLoggerFactory locLoggerFactory;
    private final IMessageConveyor messageConveyor;

    private LoggerFactory(String subsystem) {
        this.messageConveyor = MessageConveyorFactory.messageConveyerFactory().getDefaultMessageConveyer(subsystem);
        this.locLoggerFactory = new LocLoggerFactory(messageConveyor);
    }

    public LocLogger getLogger(Category category) {
        return locLoggerFactory.getLocLogger(category.getName());
    }

    public XLogger getXLogger(Category category) {
        return XLoggerFactory.getXLogger(category.getName());
    }

    public static LoggerFactory loggerFactory() {
        if (INSTANCE == null) {
            synchronized (LoggerFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoggerFactory("WELD");
                }
            }
        }
        return INSTANCE;
    }

    public IMessageConveyor getMessageConveyor() {
        return messageConveyor;
    }

}
