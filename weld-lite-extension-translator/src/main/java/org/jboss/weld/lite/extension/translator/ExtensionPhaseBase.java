package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

abstract class ExtensionPhaseBase {
    private final ExtensionPhase phase;

    final jakarta.enterprise.inject.spi.BeanManager beanManager;
    final ExtensionInvoker util;
    final SharedErrors errors;

    ExtensionPhaseBase(ExtensionPhase phase, jakarta.enterprise.inject.spi.BeanManager beanManager,
            ExtensionInvoker util, SharedErrors errors) {
        this.phase = phase;

        this.beanManager = beanManager;
        this.util = util;
        this.errors = errors;
    }

    final void run() {
        List<java.lang.reflect.Method> extensionMethods = util.findExtensionMethods(phase.annotation);

        for (java.lang.reflect.Method method : extensionMethods) {
            try {
                runExtensionMethod(method);
            } catch (DefinitionException | DeploymentException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw LiteExtensionTranslatorLogger.LOG.problemExecutingExtensionMethod(method, phase, e.getCause().toString(),
                        e);
            } catch (Exception e) {
                // we treat every other error as deployment error
                throw LiteExtensionTranslatorLogger.LOG.problemExecutingExtensionMethod(method, phase, e.toString(), e);

            }
        }
    }

    // complex phases may override, but this is enough for the simple phases
    void runExtensionMethod(java.lang.reflect.Method method) throws ReflectiveOperationException {
        int numParameters = method.getParameterCount();
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            parameter.verifyAvailable(phase, method);
        }

        List<Object> arguments = new ArrayList<>(numParameters);
        for (ExtensionMethodParameterType parameter : parameters) {
            Object argument = argumentForExtensionMethod(parameter, method);
            arguments.add(argument);
        }

        util.callExtensionMethod(method, arguments);
    }

    // all phases should override and use this as a fallback
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.MESSAGES) {
            return new MessagesImpl(errors);
        }

        throw LiteExtensionTranslatorLogger.LOG.invalidExtensionMethodParameterType(type, method.getDeclaringClass(),
                method.getName());
    }
}
