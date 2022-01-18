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

import jakarta.enterprise.inject.spi.ObserverMethod;

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
 * <p>
 * Message Ids: 000100 - 000199
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface BootstrapLogger extends WeldLogger {

    BootstrapLogger LOG = Logger.getMessageLogger(BootstrapLogger.class, Category.BOOTSTRAP.getName());

    BootstrapLogger TRACKER_LOG = Logger.getMessageLogger(BootstrapLogger.class, Category.BOOTSTRAP_TRACKER.getName());

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

    @Message(id = 116, value = "Manager has not been initialized")
    IllegalStateException managerNotInitialized();

    @Message(id = 117, value = "Required service {0} has not been specified for {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unspecifiedRequiredService(Object service, Object target);

    @Message(id = 118, value = "Only normal scopes can be passivating. Scope {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException passivatingNonNormalScopeIllegal(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 119, value = "Not generating any bean definitions from {0} because of underlying class loading error: Type {1} not found.  If this is unexpected, enable DEBUG logging to see the full error.", format = Format.MESSAGE_FORMAT)
    void ignoringClassDueToLoadingError(Object param1, Object param2);

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

    @Message(id = 130, value = "Cannot replace AnnotatedType for {0} with AnnotatedType for {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException annotatedTypeJavaClassMismatch(Object param1, Object param2);

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
    @Message(id = 139, value = "Ignoring portable extension class {0} because of underlying class loading error: Type {1} not found. Enable DEBUG logging level to see the full error.", format = Format.MESSAGE_FORMAT)
    void ignoringExtensionClassDueToLoadingError(String className, String missingDependency);

    @Message(id = 140, value = "Calling Bootstrap method after container has already been initialized. For correct order, see CDI11Bootstrap's documentation.")
    IllegalStateException callingBootstrapMethodAfterContainerHasBeenInitialized();

    @SuppressWarnings({ "weldlog:method-sig" })
    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 141, value = "Falling back to the default observer method resolver due to {0}", format = Format.MESSAGE_FORMAT)
    void notUsingFastResolver(ObserverMethod<?> observer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 142, value = "Exception loading annotated type using ClassFileServices. Falling back to the default implementation. {0}", format = Format.MESSAGE_FORMAT)
    void exceptionLoadingAnnotatedType(String message);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE, value = "No PAT observers resolved for {0}. Skipping.", format = Format.MESSAGE_FORMAT)
    void patSkipped(SlimAnnotatedType<?> type);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE, value = "Sending PAT using the default event resolver: {0}", format = Format.MESSAGE_FORMAT)
    void patDefaultResolver(SlimAnnotatedType<?> type);

    @LogMessage(level = Logger.Level.TRACE)
    @Message(id = Message.NONE, value = "Sending PAT using the fast event resolver: {0}", format = Format.MESSAGE_FORMAT)
    void patFastResolver(SlimAnnotatedType<?> type);

    @Message(id = 143, value = "Container lifecycle event method invoked outside of extension observer method invocation.")
    IllegalStateException containerLifecycleEventMethodInvokedOutsideObserver();

    @Message(id = 144, value = "CDI API version mismatch. CDI 1.0 API detected on classpath. Weld requires version 1.1 or better.")
    IllegalStateException cdiApiVersionMismatch();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 145, value = "Bean identifier index built:\n  {0}", format = Format.MESSAGE_FORMAT)
    void beanIdentifierIndexBuilt(Object info);

    @LogMessage(level = Level.WARN)
    @Message(id = 146, value = "BeforeBeanDiscovery.addAnnotatedType(AnnotatedType<?>) used for {0} is deprecated from CDI 1.1!", format = Format.MESSAGE_FORMAT)
    void deprecatedAddAnnotatedTypeMethodUsed(Class<?> clazz);

    @LogMessage(level = Level.WARN)
    @Message(id = 147, value = "Decorator {0} declares inappropriate constructor therefore will not available as a managed bean!", format = Format.MESSAGE_FORMAT)
    void decoratorWithNonCdiConstructor(String clazzName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 148, value = "ProcessAnnotatedType.setAnnotatedType() called by {0}: {1} replaced by {2}", format = Format.MESSAGE_FORMAT)
    void setAnnotatedTypeCalled(Object extensionName, Object original, Object newer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 149, value = "ProcessBeanAttributes.setBeanAttributes() called by {0}: {1} replaced by {2}", format = Format.MESSAGE_FORMAT)
    void setBeanAttributesCalled(Object extensionName, Object original, Object newer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 150, value = "ProcessInjectionPoint.setInjectionPoint() called by {0}: {1} replaced by {2}", format = Format.MESSAGE_FORMAT)
    void setInjectionPointCalled(Object extensionName, Object original, Object newer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 151, value = "ProcessInjectionTarget.setInjectionTarget() called by {0}: {1} replaced by {2}", format = Format.MESSAGE_FORMAT)
    void setInjectionTargetCalled(Object extensionName, Object original, Object newer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 152, value = "ProcessProducer.setProducer() called by {0}: {1} replaced by {2}", format = Format.MESSAGE_FORMAT)
    void setProducerCalled(Object extensionName, Object original, Object newer);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 153, value = "AfterTypeDiscovery.addAnnotatedType() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addAnnotatedTypeCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 154, value = "AfterBeanDiscovery.addBean() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addBeanCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 155, value = "AfterBeanDiscovery.addObserverMethod() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addObserverMethodCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 156, value = "AfterBeanDiscovery.addContext() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addContext(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 157, value = "AfterBeanDiscovery.addDefinitionError() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addDefinitionErrorCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 158, value = "BeforeBeanDiscovery.addQualifier() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addQualifierCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 159, value = "BeforeBeanDiscovery.addScope() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addScopeCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 160, value = "BeforeBeanDiscovery.addStereoType() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addStereoTypeCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 161, value = "BeforeBeanDiscovery.addInterceptorBindingCalled() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addInterceptorBindingCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 162, value = "BeforeBeanDiscovery.addAnnotatedType() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void addAnnotatedTypeCalledInBBD(Object extensionName, Object type);

    @Message(id = 163, value = "Non-unique bean deployment identifier detected: {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException nonuniqueBeanDeploymentIdentifier(Object info);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 164, value = "ProcessAnnotatedType.veto() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void annotatedTypeVetoed(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 165, value = "ProcessBeanAttributes.veto() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void beanAttributesVetoed(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 166, value = "AfterTypeDiscovery.{3} modified by {0} {2} {1}", format = Format.MESSAGE_FORMAT)
    void typeModifiedInAfterTypeDiscovery(Object extensionName, Object type, Object operation, Object types);

    @LogMessage(level = Level.WARN)
    @Message(id = 167, value = "Class {0} is annotated with @{1} but it does not declare an appropriate constructor therefore is not registered as a bean!", format = Format.MESSAGE_FORMAT)
    void annotatedTypeNotRegisteredAsBeanDueToMissingAppropriateConstructor(String clazzName, String annotationName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 168, value = "Extension bean deployed: {0}", format = Format.MESSAGE_FORMAT)
    void extensionBeanDeployed(Object extension);

    @LogMessage(level = Level.INFO)
    @Message(id = 169, value = "Jandex cannot distinguish inner and static nested classes! Update Jandex to 2.0.3.Final version or newer to improve scanning performance.", format = Format.MESSAGE_FORMAT)
    void usingOldJandexVersion();

    @Message(id = 170, value = "{0} observer cannot call both the configurator and set methods. Extension {1} \nStackTrace:", format = Format.MESSAGE_FORMAT)
    IllegalStateException configuratorAndSetMethodBothCalled(Object observerName, Object extension);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 171, value = "BeforeBeanDiscovery.configureQualifier() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureQualifierCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 172, value = "BeforeBeanDiscovery.configureInterceptorBinding() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureInterceptorBindingCalled(Object extensionName, Object type);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 173, value = "ProcessProducer.configureProducer() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureProducerCalled(Object extensionName, Object bean);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 174, value = "ProcessBeanAttributes.configureBeanAttributes() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureBeanAttributesCalled(Object extensionName, Object bean);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 175, value = "ProcessBeanAttributes.isIgnoreFinalMethods() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void ignoreFinalMethodsCalled(Object extensionName, Object bean);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 176, value = "ProcessAnnotatedType.configureAnnotatedType() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureAnnotatedTypeCalled(Object extensionName, Object bean);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 177, value = "ProcessObserverMethod.configureObserverMethod() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureObserverMethodCalled(Object extensionName, Object bean);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 178, value = "ProcessInjectionPoint.configureInjectionPoint() called by {0} for {1}", format = Format.MESSAGE_FORMAT)
    void configureInjectionPointCalled(Object extensionName, Object bean);

    @Message(id = 179, value = "{0} created by {1} cannot be processed", format = Format.MESSAGE_FORMAT)
    DeploymentException unableToProcessConfigurator(Object configurator, Object extensionName, @Cause Throwable cause);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 180, value = "Drop unused bean metadata: {0}", format = Format.MESSAGE_FORMAT)
    void dropUnusedBeanMetadata(Object bean);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 181, value = "org.jboss.weld.executor.threadPoolType=COMMON detected but ForkJoinPool.commonPool() does not work with SecurityManager enabled, switching to {0} thread pool", format = Format.MESSAGE_FORMAT)
    void commonThreadPoolWithSecurityManagerEnabled(Object threadPoolType);

    // id 182 was used in Weld 3 and 4 and removed for Weld 5

    @Message(id = 183, value= "Multiple different @Priority values derived from stereotype annotations for annotated type - {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException multiplePriorityValuesDeclared(Object annotatedType);

}
