package org.jboss.weld.environment.se.logging;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.environment.logging.Category;
import org.jboss.weld.environment.logging.WeldEnvironmentLogger;
import org.jboss.weld.exceptions.IllegalArgumentException;

/**
 * A source of localized log/bundle messages and exceptions. Note that this interface extends {@link WeldEnvironmentLogger} so
 * that regular logger methods are
 * available.
 *
 * Message IDs: 002000 - 002099
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Kirill Gaevskii
 */
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface WeldSELogger extends WeldEnvironmentLogger {

    WeldSELogger LOG = Logger.getMessageLogger(WeldSELogger.class, Category.BOOTSTRAP.getName());

    @Message(id = 2000, value = "Weld SE container {0} is already running!", format = Format.MESSAGE_FORMAT)
    IllegalStateException weldContainerAlreadyRunning(Object id);

    @LogMessage(level = Level.INFO)
    @Message(id = 2001, value = "Weld SE container {0} shut down", format = Format.MESSAGE_FORMAT)
    void weldContainerShutdown(Object id);

    @Message(id = 2002, value = "Weld SE container {0} was already shut down", format = Format.MESSAGE_FORMAT)
    IllegalStateException weldContainerAlreadyShutDown(Object id);

    @LogMessage(level = Level.INFO)
    @Message(id = 2003, value = "Weld SE container {0} initialized", format = Format.MESSAGE_FORMAT)
    void weldContainerInitialized(Object id);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 2004, value = "Scanning package {0}, class resource: {1}", format = Format.MESSAGE_FORMAT)
    void scanningPackage(Object packageName, Object resource);

    @LogMessage(level = Level.WARN)
    @Message(id = 2005, value = "Package intended for scanning was not found: {0}", format = Format.MESSAGE_FORMAT)
    void packageNotFound(Object packageName);

    @LogMessage(level = Level.INFO)
    @Message(id = 2006, value = "Multiple containers running - CDI.current() may not work properly: {0}", format = Format.MESSAGE_FORMAT)
    void multipleContainersRunning(Object ids);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 2007, value = "Resource URL procotol not supported: {0}", format = Format.MESSAGE_FORMAT)
    void resourceUrlProtocolNotSupported(Object resource);

    @LogMessage(level = Level.WARN)
    @Message(id = 2008, value = "Bean class {0} found in multiple bean archives - this may result in incorrect behavior: {1}", format = Format.MESSAGE_FORMAT)
    void beanClassDeployedInMultipleBeanArchives(Object beanClass, Object bdas);

    @Message(id = 2009, value = "Weld SE container cannot be initialized - no bean archives found")
    IllegalStateException weldContainerCannotBeInitializedNoBeanArchivesFound();

    @Message(id = 2010, value = "The observed type {0} is not a container lifecycle event type", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException observedTypeNotContonainerLifecycleEventType(Object type);

    @Message(id = 2011, value = "The observed type {0} does not match the container lifecycle event type {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException observedTypeDoesNotMatchContonainerLifecycleEventType(Object type, Object eventType);

    @Message(id = 2012, value = "The container lifecycle observer is not properly initialized: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException containerLifecycleObserverNotInitialized(Object info);

    // 2013 used in Weld 2.3

    @Message(id = 2014, value = "Weld SE container with id {0} has not yet validated the deployment - methods for programmatic lookup cannot be used", format = Format.MESSAGE_FORMAT)
    IllegalStateException weldContainerDeploymentNotValidated(Object id);

    @Message(id = 2015, value = "Bean discovery mode NONE is not a valid option for Weld SE deployment archive - Weld SE container with id {0}.", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException beanArchiveWithModeNone(Object id);

    @Message(id = 2016, value = "Zero or more than one container is running - WeldContainer.current() cannot determine the current container.", format = Format.MESSAGE_FORMAT)
    IllegalStateException zeroOrMoreThanOneContainerRunning();

    @Message(id = 2017, value = "Unexpected value for parameter 'org.jboss.weld.se.additionalBeanDefiningAnnotations'. Expected java.util.Collection but found {0}. ", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException unexpectedValueForAdditionalBeanDefiningAnnotations(Class clazz);

    @LogMessage(level = Level.WARN)
    @Message(id = 2018, value = "Skipping registration of additional bean defining annotation via `org.jboss.weld.se.additionalBeanDefiningAnnotations`. "
            +
            "Only values of type Class<? extends Annotation> are valid. Found: {0}", format = Format.MESSAGE_FORMAT)
    void unexpectedItemsInValueCollection(Class clazz);

    @Message(id = 2019, value = "Failed to parse the following string as additional bean defining annotation: {0}. The exception was: {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException failedToLoadClass(String className, String exception);
}
