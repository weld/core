package org.jboss.weld.lite.extension.translator;

import java.util.function.Supplier;

import jakarta.enterprise.inject.build.compatible.spi.InvokerValidation;

import org.jboss.weld.invokable.AsyncHandlerRegistry;
import org.jboss.weld.logging.InvokerLogger;
import org.jboss.weld.manager.api.WeldManager;

class InvokerValidationImpl implements InvokerValidation {

    private final WeldManager beanManager;
    private final SharedErrors errors;

    InvokerValidationImpl(WeldManager beanManager, SharedErrors errors) {
        this.beanManager = beanManager;
        this.errors = errors;
    }

    @Override
    public void ensureAsyncHandlerExists(Class<?> asyncType, Supplier<String> message) {
        if (!beanManager.getServices().get(AsyncHandlerRegistry.class).hasHandler(asyncType)) {
            String msg = message != null ? message.get() : null;
            String suffix = msg != null ? ": " + msg : "";
            errors.list.add(InvokerLogger.LOG.asyncHandlerNotFound(asyncType, suffix));
        }
    }
}
