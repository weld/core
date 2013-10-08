/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Log messages for bootstrap
 *
 * Message Ids: 000100 - 000199
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface BootstrapLogger extends WeldLogger {

    BootstrapLogger LOG = Logger.getMessageLogger(BootstrapLogger.class, Category.BOOTSTRAP.getName());

    @LogMessage(level = Level.DEBUG)
    @Message(id = 100, value = "Weld initialized. Validating beans")
    void validatingBeans();

    @LogMessage(level = Level.INFO)
    @Message(id = 101, value = "Transactional services not available. Injection of @Inject UserTransaction not available. Transactional observers will be invoked synchronously.")
    void jtaUnavailable();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 103, value = "Enabled alternatives for {0}: {1}", format = Format.MESSAGE_FORMAT)
    void enabledAlternatives(Object param1, Object param2);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 104, value = "Enabled decorator types for {0}: {1}", format = Format.MESSAGE_FORMAT)
    void enabledDecorators(Object param1, Object param2);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 105, value = "Enabled interceptor types for {0}: {1}", format = Format.MESSAGE_FORMAT)
    void enabledInterceptors(Object param1, Object param2);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 106, value = "Bean: {0}", format = Format.MESSAGE_FORMAT)
    void foundBean(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 107, value = "Interceptor: {0}", format = Format.MESSAGE_FORMAT)
    void foundInterceptor(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 108, value = "Decorator: {0}", format = Format.MESSAGE_FORMAT)
    void foundDecorator(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 109, value = "ObserverMethod: {0}", format = Format.MESSAGE_FORMAT)
    void foundObserverMethod(Object param1);

    @Message(id = 110, value = "Cannot set the annotation type to null (if you want to stop the type being used, call veto()):  {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException annotationTypeNull(Object param1);

    @Message(id = 111, value = "Bean type is not STATELESS, STATEFUL or SINGLETON:  {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException beanTypeNotEjb(Object param1);

    @Message(id = 112, value = "Class {0} has both @Interceptor and @Decorator annotations", format = Format.MESSAGE_FORMAT)
    DefinitionException beanIsBothInterceptorAndDecorator(Object param1);

    @Message(id = 113, value = "BeanDeploymentArchive must not be null:  {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException deploymentArchiveNull(Object param1);

    @Message(id = 114, value = "Must start the container with a deployment")
    IllegalArgumentException deploymentRequired();

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 115, value = "No application context BeanStore set")
    String beanStoreMissing();

    @Message(id = 116, value = "Manager has not been initialized")
    IllegalStateException managerNotInitialized();

    @Message(id = 117, value = "Required service {0} has not been specified", format = Format.MESSAGE_FORMAT)
    IllegalStateException unspecifiedRequiredService(Object param1);

    @Message(id = 118, value = "Only normal scopes can be passivating. Scope {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException passivatingNonNormalScopeIllegal(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 119, value = "Not generating any bean definitions from {0} because of underlying class loading error: Type {1} not found.  If this is unexpected, enable DEBUG logging to see the full error.", format = Format.MESSAGE_FORMAT)
    void ignoringClassDueToLoadingError(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 120, value = "Enums have already been injected")
    String enumsAlreadyInjected();

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 122, value = "Unable to create InjectionTarget for {0}", format = Format.MESSAGE_FORMAT)
    String enumInjectionTargetNotCreated(Object param1);

    @Message(id = 123, value = "Error loading {0} defined in {1}", format = Format.MESSAGE_FORMAT)
    DeploymentException errorLoadingBeansXmlEntry(Object param1, Object param2, @Cause Throwable cause);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 124, value = "Using {0} threads for bootstrap", format = Format.MESSAGE_FORMAT)
    void threadsInUse(Object param1);

    @Message(id = 125, value = "Invalid thread pool size: {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException invalidThreadPoolSize(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 126, value = "Timeout shutting down thread pool {0} at {1}", format = Format.MESSAGE_FORMAT)
    void timeoutShuttingDownThreadPool(Object param1, Object param2);

    @Message(id = 127, value = "Invalid thread pool type: {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException invalidThreadPoolType(Object param1);

    @Message(id = 128, value = "Invalid value for property {0}: {1}", format = Format.MESSAGE_FORMAT)
    DeploymentException invalidPropertyValue(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 129, value = "Two AnnotatedType implementations with the same id: {0}, {1}, {2}", format = Format.MESSAGE_FORMAT)
    String duplicateAnnotatedTypeId(Object param1, Object param2, Object param3);

    @Message(id = 130, value = "Cannot replace AnnotatedType for {0} with AnnotatedType for {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException annotatedTypeJavaClassMismatch(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 131, value = "Priority {0} specified at {1} is outside of the recommended range (0 - 3099)", format = Format.MESSAGE_FORMAT)
    String priorityOutsideOfRecommendedRange(Object param1, Object param2);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 132, value = "Disabled alternative (ignored): {0}", format = Format.MESSAGE_FORMAT)
    void foundDisabledAlternative(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 133, value = "Specialized bean (ignored): {0}", format = Format.MESSAGE_FORMAT)
    void foundSpecializedBean(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 134, value = "Producer (method or field) of specialized bean (ignored): {0}", format = Format.MESSAGE_FORMAT)
    void foundProducerOfSpecializedBean(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 135, value = "Legacy deployment metadata provided by the integrator. Certain functionality will not be available.")
    void legacyDeploymentMetadataProvided();

    @LogMessage(level = Level.ERROR)
    @Message(id = 136, value = "Exception(s) thrown during observer of BeforeShutdown: ")
    void exceptionThrownDuringBeforeShutdownObserver();

    @LogMessage(level = Level.TRACE)
    @Message(id = 137, value = "Exception while loading class '{0}' : {1}", format = Format.MESSAGE_FORMAT)
    void exceptionWhileLoadingClass(Object param1, Object param2);

    @LogMessage(level = Level.TRACE)
    @Message(id = 138, value = "Error while loading class '{0}' : {1}", format = Format.MESSAGE_FORMAT)
    void errorWhileLoadingClass(Object param1, Object param2);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 139, value = "Ignoring portable extension class {0} because of underlying class loading error: Type {1} not found. Enable DEBUG logging level to see the full error." , format = Format.MESSAGE_FORMAT)
    void ignoringExtensionClassDueToLoadingError(String className, String missingDependency);

    @Message(id = 140, value = "Calling Bootstrap method after container has already been initialized. For correct order, see CDI11Bootstrap's documentation.")
    IllegalStateException callingBootstrapMethodAfterContainerHasBeenInitialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 141, value = "Falling back to the default observer method resolver due to {0}", format = Format.MESSAGE_FORMAT)
    void notUsingFastResolver(ObserverMethod<?> observer);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 142, value = "Exception loading annotated type using ClassFileServices. Falling back to the default implementation. {0}", format = Format.MESSAGE_FORMAT)
    void exceptionLoadingAnnotatedType(String message);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE , value = "No PAT observers resolved for {0}. Skipping.", format = Format.MESSAGE_FORMAT)
    void patSkipped(SlimAnnotatedType<?> type);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE , value = "Sending PAT using the default event resolver: {0}", format = Format.MESSAGE_FORMAT)
    void patDefaultResolver(SlimAnnotatedType<?> type);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE , value = "Sending PAT using the fast event resolver: {0}", format = Format.MESSAGE_FORMAT)
    void patFastResolver(SlimAnnotatedType<?> type);

}