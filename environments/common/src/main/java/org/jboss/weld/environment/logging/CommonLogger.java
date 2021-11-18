/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.logging;


import javax.enterprise.inject.UnsatisfiedResolutionException;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.resources.spi.ClassFileInfoException;

/**
 *
 * Message IDs: 000002 - 000099
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Kirill Gaevskii
 */
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface CommonLogger extends WeldEnvironmentLogger {

    CommonLogger LOG = Logger.getMessageLogger(CommonLogger.class, Category.BOOTSTRAP.getName());

    @LogMessage(level = Level.WARN)
    @Message(id = 2, value = "Could not read resource with name: {0}", format = Format.MESSAGE_FORMAT)
    void couldNotReadResource(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 4, value = "Could not invoke JNLPClassLoader#getJarFile(URL) on context class loader, expecting Web Start class loader", format = Format.MESSAGE_FORMAT)
    void unexpectedClassLoader(@Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 5, value = "JNLPClassLoader#getJarFile(URL) threw exception", format = Format.MESSAGE_FORMAT)
    void jnlpClassLoaderInternalException(@Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 6, value = "Could not invoke JNLPClassLoader#getJarFile(URL) on context class loader", format = Format.MESSAGE_FORMAT)
    void jnlpClassLoaderInvocationException(@Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 7, value = "Error handling file path\n  File: {0}\n  Path: {1}", format = Format.MESSAGE_FORMAT)
    void cannotHandleFilePath(Object file, Object path, @Cause Throwable cause);

    // log message with id 8 was removed

    @LogMessage(level = Level.WARN)
    @Message(id = 10, value = "Could not open the stream on the url {0} when adding to the jandex index.", format = Format.MESSAGE_FORMAT)
    void couldNotOpenStreamForURL(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 11, value = "Could not close the stream on the url {0} when adding to the jandex index.", format = Format.MESSAGE_FORMAT)
    void couldNotCloseStreamForURL(Object param1, @Cause Throwable cause);

    @Message(id = 12, value = "Unable to load class {0}", format = Format.MESSAGE_FORMAT)
    ClassFileInfoException unableToLoadClass(Object param1);

    @Message(id = 13, value = "beans.xml defines unrecognized bean-discovery-mode value: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException undefinedBeanDiscoveryValue(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 14, value = "Falling back to Java Reflection for bean-discovery-mode=\"annotated\" discovery. Add org.jboss:jandex to the classpath to speed-up startup.", format = Format.MESSAGE_FORMAT)
    void reflectionFallback();

    @Message(id = 15, value = "Unable to load annotation: {0}", format = Format.MESSAGE_FORMAT)
    ClassFileInfoException unableToLoadAnnotation(Object param1);

    @Message(id = 16, value = "Missing beans.xml file in META-INF", format = Format.MESSAGE_FORMAT)
    IllegalStateException missingBeansXml();

    @Message(id = 18, value = "Unable to resolve a bean for {0} with bindings {1}", format = Format.MESSAGE_FORMAT)
    UnsatisfiedResolutionException unableToResolveBean(Object param1, Object param2);

    @Message(id = 19, value = "Jandex index is null in the constructor of class: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException jandexIndexNotCreated(Object param1);

    @LogMessage(level = Level.INFO)
    @Message(id = 20, value = "Using jandex for bean discovery", format = Format.MESSAGE_FORMAT)
    void usingJandex();

    // log message with id 21 was removed
    // log message with id 22 was removed

    @LogMessage(level = Level.DEBUG)
    @Message(id = 23, value = "Archive isolation disabled - only one bean archive will be created", format = Format.MESSAGE_FORMAT)
    void archiveIsolationDisabled();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 24, value = "Archive isolation enabled - creating multiple isolated bean archives if needed", format = Format.MESSAGE_FORMAT)
    void archiveIsolationEnabled();

    @Message(id = 25, value = "Index for name: {0} not found.", format = Format.MESSAGE_FORMAT)
    ClassFileInfoException indexForNameNotFound(Object param1);

    @Message(id = 26, value = "Unable to instantiate {0} using parameters: {1}.", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToInstantiate(Object param1, Object param2, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 28, value = "Weld initialization skipped - no bean archive found")
    void initSkippedNoBeanArchiveFound();

    @Message(id = 29, value = "Cannot load class for {0}.", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotLoadClass(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 30, value = "Cannot load class using the ResourceLoader: {0}", format = Format.MESSAGE_FORMAT)
    void cannotLoadClassUsingResourceLoader(String className);

    @LogMessage(level = Level.WARN)
    @Message(id = 31, value = "The bean archive reference {0} cannot be handled by any BeanArchiveHandler: {1}", format = Format.MESSAGE_FORMAT)
    void beanArchiveReferenceCannotBeHandled(Object beanArchiveRef, Object handlers);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 32, value = "Processing bean archive reference: {0}", format = Format.MESSAGE_FORMAT)
    void processingBeanArchiveReference(Object beanArchiveRef);

    @Message(id = 33, value = "Invalid bean archive scanning result - found multiple results with the same reference: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException invalidScanningResult(Object beanArchiveRef);

    @Message(id = 34, value = "Cannot scan class path entry: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotScanClassPathEntry(Object entry, @Cause Throwable cause);

    @Message(id = 35, value = "Class path entry cannot be read: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotReadClassPathEntry(Object entry);

    @Message(id = 36, value = "Weld cannot read the java class path system property!", format = Format.MESSAGE_FORMAT)
    IllegalStateException cannotReadJavaClassPathSystemProperty();

    @Message(id = 37, value = "Unable to initialize the Probe component: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToInitializeProbeComponent(Object component, @Cause Throwable cause);

    @Message(id = 38, value = "Development mode is enabled but the following Probe component is not found on the classpath: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException probeComponentNotFoundOnClasspath(Object component);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 39, value = "Bean archive reference {0} handled by {1}", format = Format.MESSAGE_FORMAT)
    void beanArchiveReferenceHandled(Object beanArchiveRef, Object handler);

    @LogMessage(level = Level.INFO)
    @Message(id = 40, value = "Jandex discovery strategy was disabled.", format = Format.MESSAGE_FORMAT)
    void jandexDiscoveryStrategyDisabled();

    @LogMessage(level = Level.INFO)
    @Message(id = 41, value = "Using {0} for bean discovery", format = Format.MESSAGE_FORMAT)
    void usingServiceLoaderSourcedDiscoveryStrategy(Object discoveryStrategy);

    @LogMessage(level = Level.WARN)
    @Message(id = 42, value = "Class path entry does not exist: {0}", format = Format.MESSAGE_FORMAT)
    void classPathEntryDoesNotExist(Object entry);

}
