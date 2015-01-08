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
package org.jboss.weld.probe;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 *
 * @author Martin Kouba
 */
@SuppressWarnings("checkstyle:magicnumber")
@MessageLogger(projectCode = "PROBE-")
public interface ProbeLogger extends BasicLogger {

    ProbeLogger LOG = Logger.getMessageLogger(ProbeLogger.class, "org.jboss.weld.probe.Probe");

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1, value = "Resource path {0} matched for {1}", format = Format.MESSAGE_FORMAT)
    void resourcePathMatched(Object resource, String pathInfo);

    @LogMessage(level = Level.INFO)
    @Message(id = 2, value = "Processing bean deployment archive: {0}", format = Format.MESSAGE_FORMAT)
    void processingBeanDeploymentArchive(Object archive);

    @LogMessage(level = Level.WARN)
    @Message(id = 3, value = "A problem occured during contextual instance introspection: {0}", format = Format.MESSAGE_FORMAT)
    void introspectionProblem(Object bean, @Cause Throwable cause);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 4, value = "Filters applied: {0}", format = Format.MESSAGE_FORMAT)
    void filtersApplied(Object filters);

    @Message(id = 5, value = "Probe is not properly initialized")
    IllegalStateException probeNotInitialized();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 6, value = "{0} is excluded from monitoring", format = Format.MESSAGE_FORMAT)
    void invocationMonitorNotAssociated(Object beanClass);
}
