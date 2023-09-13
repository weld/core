package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

class MessagesImpl implements Messages {

    private final String AT = " at ";

    private final SharedErrors errors;

    MessagesImpl(SharedErrors errors) {
        this.errors = errors;
    }

    @Override
    public void info(String message) {
        LiteExtensionTranslatorLogger.LOG.info(message);
    }

    @Override
    public void info(String message, AnnotationTarget relatedTo) {
        LiteExtensionTranslatorLogger.LOG.info(message + AT + relatedTo);
    }

    @Override
    public void info(String message, BeanInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.info(message + AT + relatedTo);
    }

    @Override
    public void info(String message, ObserverInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.info(message + AT + relatedTo);
    }

    @Override
    public void warn(String message) {
        LiteExtensionTranslatorLogger.LOG.warn(message);
    }

    @Override
    public void warn(String message, AnnotationTarget relatedTo) {
        LiteExtensionTranslatorLogger.LOG.warn(message + AT + relatedTo);
    }

    @Override
    public void warn(String message, BeanInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.warn(message + AT + relatedTo);
    }

    @Override
    public void warn(String message, ObserverInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.warn(message + AT + relatedTo);
    }

    @Override
    public void error(String message) {
        LiteExtensionTranslatorLogger.LOG.error(message);
        errors.list.add(new DeploymentException(message));
    }

    @Override
    public void error(String message, AnnotationTarget relatedTo) {
        LiteExtensionTranslatorLogger.LOG.error(message + AT + relatedTo);
        errors.list.add(new DeploymentException(message + AT + relatedTo));
    }

    @Override
    public void error(String message, BeanInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.error(message + AT + relatedTo);
        errors.list.add(new DeploymentException(message + AT + relatedTo));
    }

    @Override
    public void error(String message, ObserverInfo relatedTo) {
        LiteExtensionTranslatorLogger.LOG.error(message + AT + relatedTo);
        errors.list.add(new DeploymentException(message + AT + relatedTo));
    }

    @Override
    public void error(Exception exception) {
        errors.list.add(exception);
    }
}
