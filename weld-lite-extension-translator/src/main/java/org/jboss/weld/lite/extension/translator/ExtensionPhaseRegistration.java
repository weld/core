package org.jboss.weld.lite.extension.translator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.inject.build.compatible.spi.Registration;

import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

class ExtensionPhaseRegistration extends ExtensionPhaseBase {
    private final List<ExtensionPhaseRegistrationAction> actions;

    ExtensionPhaseRegistration(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, List<ExtensionPhaseRegistrationAction> actions) {
        super(ExtensionPhase.REGISTRATION, beanManager, util, errors);
        this.actions = actions;
    }

    @Override
    void runExtensionMethod(java.lang.reflect.Method method) {
        int numParameters = method.getParameterCount();
        int numQueryParameters = 0;
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            if (parameter.isQuery()) {
                numQueryParameters++;
            }

            parameter.verifyAvailable(ExtensionPhase.REGISTRATION, method);
        }

        if (numQueryParameters == 0 || numQueryParameters > 1) {
            throw LiteExtensionTranslatorLogger.LOG.incorrectParameterCount("BeanInfo or ObserverInfo", method,
                    method.getDeclaringClass());
        }

        ExtensionMethodParameterType query = parameters.stream()
                .filter(ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        if (query == ExtensionMethodParameterType.BEAN_INFO) {
            Consumer<jakarta.enterprise.inject.spi.ProcessBean<?>> pbAcceptor = pb -> {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        jakarta.enterprise.inject.spi.AnnotatedParameter<?> disposer = null;
                        if (pb instanceof jakarta.enterprise.inject.spi.ProcessProducerField) {
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerField<?, ?>) pb)
                                    .getAnnotatedDisposedParameter();
                        } else if (pb instanceof jakarta.enterprise.inject.spi.ProcessProducerMethod) {
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerMethod<?, ?>) pb)
                                    .getAnnotatedDisposedParameter();
                        }
                        if (pb.getBean() instanceof jakarta.enterprise.inject.spi.Interceptor) {
                            jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor = (jakarta.enterprise.inject.spi.Interceptor<?>) pb
                                    .getBean();
                            argument = new InterceptorInfoImpl(cdiInterceptor, pb.getAnnotated(), beanManager);
                        } else {
                            argument = new BeanInfoImpl(pb.getBean(), pb.getAnnotated(), disposer, beanManager);
                        }
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments, e.toString(), e);
                }
            };

            Registration registration = method.getAnnotation(Registration.class);
            actions.add(new ExtensionPhaseRegistrationAction(new HashSet<>(Arrays.asList(registration.types())),
                    pbAcceptor, null));
        } else if (query == ExtensionMethodParameterType.INTERCEPTOR_INFO) {
            Consumer<jakarta.enterprise.inject.spi.ProcessBean<?>> pbAcceptor = pb -> {
                if (!(pb.getBean() instanceof jakarta.enterprise.inject.spi.Interceptor)) {
                    return;
                }

                jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor = (jakarta.enterprise.inject.spi.Interceptor<?>) pb
                        .getBean();

                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        argument = new InterceptorInfoImpl(cdiInterceptor, pb.getAnnotated(), beanManager);
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments, e.toString(), e);
                }
            };

            Registration registration = method.getAnnotation(Registration.class);
            actions.add(new ExtensionPhaseRegistrationAction(new HashSet<>(Arrays.asList(registration.types())),
                    pbAcceptor, null));
        } else if (query == ExtensionMethodParameterType.OBSERVER_INFO) {
            Consumer<jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?>> pomAcceptor = pom -> {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        boolean isSynthetic = pom instanceof jakarta.enterprise.inject.spi.ProcessSyntheticObserverMethod;
                        argument = new ObserverInfoImpl(pom.getObserverMethod(), isSynthetic ? null : pom.getAnnotatedMethod(),
                                beanManager);
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (InvocationTargetException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments,
                            e.getCause().toString(), e);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments, e.toString(), e);
                }
            };

            Registration registration = method.getAnnotation(Registration.class);
            actions.add(new ExtensionPhaseRegistrationAction(new HashSet<>(Arrays.asList(registration.types())),
                    null, pomAcceptor));
        } else {
            throw LiteExtensionTranslatorLogger.LOG.unknownQueryParameter(query);
        }
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.INVOKER_FACTORY || type == ExtensionMethodParameterType.WELD_INVOKER_FACTORY) {
            return new InvokerFactoryImpl(beanManager);
        } else if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl(beanManager);
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
