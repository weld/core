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

import javax.enterprise.inject.Vetoed;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
@SuppressWarnings("checkstyle:magicnumber")
@MessageLogger(projectCode = "PROBE-")
public interface ProbeLogger extends BasicLogger {

    ProbeLogger LOG = Logger.getMessageLogger(ProbeLogger.class, "org.jboss.weld.probe.Probe");

    String CATCHING_MARKER = "Catching";

    @LogMessage(level = Level.TRACE)
    @Message(id = 0, value = CATCHING_MARKER)
    void catchingTrace(@Cause Throwable throwable);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 1, value = "Resource {0} matched for {1}", format = Format.MESSAGE_FORMAT)
    void resourceMatched(Object resource, String pathInfo);

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
    @Message(id = 6, value = "{0} not monitored - excluded", format = Format.MESSAGE_FORMAT)
    void invocationMonitorNotAssociatedExcluded(Object beanClass);

    @LogMessage(level = Level.WARN)
    @Message(id = 8, value = "\n=====================================\n Weld Development Mode: ENABLED\n ------------------------------------\n Disable this mode in production - it may have negative impact on performance and/or represent a potential security risk\n=====================================")
    void developmentModeEnabled();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 9, value = "@MonitoredComponent stereotype added to {0}", format = Format.MESSAGE_FORMAT)
    void monitoringStereotypeAdded(Object bean);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 10, value = "{0} not monitored - non-proxyable type", format = Format.MESSAGE_FORMAT)
    void invocationMonitorNotAssociatedNonProxyableType(Object type);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 11, value = "Event {0} not monitored - excluded", format = Format.MESSAGE_FORMAT)
    void eventExcluded(Object type);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 12, value = "{0} monitoring limit {1} exceed - some old data were removed", format = Format.MESSAGE_FORMAT)
    void monitoringLimitExceeded(Object monitor, Object limit);

    @Message(id = 13, value = "Probe filter is not able to operate - missing {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException probeFilterUnableToOperate(Class<?> component);

    @Message(id = 14, value = "Cannot register a Probe MBean {0} for: {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToRegisterMBean(Object mbean, Object context, @Cause Throwable cause);

    @Message(id = 15, value = "Cannot unregister a Probe MBean {0} for: {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToUnregisterMBean(Object mbean, Object context, @Cause Throwable cause);

    @Message(id = 16, value = "Unable to parse query filters: {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException unableToParseQueryFilter(Object filters);

    @LogMessage(level = Level.WARN)
    @Message(id = 17, value = "Access to {0} denied for {1}", format = Format.MESSAGE_FORMAT)
    void requestDenied(Object requestInfo, Object remoteAddr);

    @LogMessage(level = Level.WARN)
    @Message(id = 18, value = "Export path does not exist or is not writable: {0}", format = Format.MESSAGE_FORMAT)
    void invalidExportPath(Object path);

    @LogMessage(level = Level.WARN)
    @Message(id = 19, value = "Unable to export data to {0}: {1}", format = Format.MESSAGE_FORMAT)
    void unableToExportData(Object path, Object cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 20, value = "A problem occured during monitoring of bean instance construction: {0}", format = Format.MESSAGE_FORMAT)
    void aroundConstructMonitoringProblem(Object bean, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 21, value = "\n=====================================\n Weld - Deployment Validation: FAILED \n ------------------------------------\n HTML report generated to: \n\n {0} \n=====================================", format = Format.MESSAGE_FORMAT)
    void validationReportExported(Object path);

    @LogMessage(level = Level.WARN)
    @Message(id = 22, value = "Probe was not allowed to use customized Annotation toString(), falling back to JDK's default Annotation.toString(). The exception was: {0}", format = Format.MESSAGE_FORMAT)
    void cannotUseUnifiedAnnotationToStringConversion(Object path);
}
