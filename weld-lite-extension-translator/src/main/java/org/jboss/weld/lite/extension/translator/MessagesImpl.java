package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;

import java.util.logging.Logger;

class MessagesImpl implements Messages {
    private final SharedErrors errors;
    private final Logger logger;

    MessagesImpl(java.lang.reflect.Method method, SharedErrors errors) {
        this.errors = errors;
        this.logger = Logger.getLogger(method.getDeclaringClass().getName());
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, AnnotationTarget relatedTo) {
        logger.info(message + " at " + relatedTo);
    }

    @Override
    public void info(String message, BeanInfo relatedTo) {
        logger.info(message + " at " + relatedTo);
    }

    @Override
    public void info(String message, ObserverInfo relatedTo) {
        logger.info(message + " at " + relatedTo);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void warn(String message, AnnotationTarget relatedTo) {
        logger.warning(message + " at " + relatedTo);
    }

    @Override
    public void warn(String message, BeanInfo relatedTo) {
        logger.warning(message + " at " + relatedTo);
    }

    @Override
    public void warn(String message, ObserverInfo relatedTo) {
        logger.warning(message + " at " + relatedTo);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
        errors.list.add(new jakarta.enterprise.inject.spi.DeploymentException(message));
    }

    @Override
    public void error(String message, AnnotationTarget relatedTo) {
        logger.severe(message + " at " + relatedTo);
        errors.list.add(new jakarta.enterprise.inject.spi.DeploymentException(message + " at " + relatedTo));
    }

    @Override
    public void error(String message, BeanInfo relatedTo) {
        logger.severe(message + " at " + relatedTo);
        errors.list.add(new jakarta.enterprise.inject.spi.DeploymentException(message + " at " + relatedTo));
    }

    @Override
    public void error(String message, ObserverInfo relatedTo) {
        logger.severe(message + " at " + relatedTo);
        errors.list.add(new jakarta.enterprise.inject.spi.DeploymentException(message + " at " + relatedTo));
    }

    @Override
    public void error(Exception exception) {
        errors.list.add(exception);
    }
}
