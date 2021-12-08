package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.spi.DefinitionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

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

        if (numQueryParameters != 1) {
            String errorMsg = " of type BeanInfo or ObserverInfo for method " + method + " @ " + method.getDeclaringClass();
            if (numQueryParameters == 0) {
                throw new DefinitionException("No parameter" + errorMsg);
            }

            if (numQueryParameters > 1) {
                throw new DefinitionException("More than 1 parameter" + errorMsg);
            }
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
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerField<?, ?>) pb).getAnnotatedDisposedParameter();
                        } else if (pb instanceof jakarta.enterprise.inject.spi.ProcessProducerMethod) {
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerMethod<?, ?>) pb).getAnnotatedDisposedParameter();
                        }

                        argument = new BeanInfoImpl(pb.getBean(), pb.getAnnotated(), disposer);
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
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

                jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor = (jakarta.enterprise.inject.spi.Interceptor<?>) pb.getBean();

                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        argument = new InterceptorInfoImpl(cdiInterceptor, pb.getAnnotated());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
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
                        argument = new ObserverInfoImpl(pom.getObserverMethod(), isSynthetic ? null : pom.getAnnotatedMethod());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };

            Registration registration = method.getAnnotation(Registration.class);
            actions.add(new ExtensionPhaseRegistrationAction(new HashSet<>(Arrays.asList(registration.types())),
                    null, pomAcceptor));
        } else {
            throw new IllegalStateException("Unknown query parameter " + query);
        }
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl();
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
