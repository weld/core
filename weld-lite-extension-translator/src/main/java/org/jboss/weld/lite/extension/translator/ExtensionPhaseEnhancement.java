package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

class ExtensionPhaseEnhancement extends ExtensionPhaseBase {
    private final List<ExtensionPhaseEnhancementAction> actions;

    ExtensionPhaseEnhancement(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, List<ExtensionPhaseEnhancementAction> actions) {
        super(ExtensionPhase.ENHANCEMENT, beanManager, util, errors);
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

            parameter.verifyAvailable(ExtensionPhase.ENHANCEMENT, method);
        }

        if (numQueryParameters == 0 || numQueryParameters > 1) {
            throw LiteExtensionTranslatorLogger.LOG.incorrectParameterCount("ClassInfo, MethodInfo, FieldInfo, ClassConfig, MethodConfig, or FieldConfig", method, method.getDeclaringClass());
        }

        ExtensionMethodParameterType query = parameters.stream()
                .filter(ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        Consumer<jakarta.enterprise.inject.spi.ProcessAnnotatedType<?>> patAcceptor = pat -> {
            // for Class{Info,Config}, there's just 1 argument list (one call);
            // for {Field,Method}{Info,Config}, there's multiple argument lists
            // (one call for each field/method)
            List<List<Object>> argumentsForAllInvocations = new ArrayList<>();
            if (query == ExtensionMethodParameterType.CLASS_INFO) {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter == ExtensionMethodParameterType.CLASS_INFO) {
                        argument = new ClassInfoImpl(pat.getAnnotatedType());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                argumentsForAllInvocations.add(arguments);
            } else if (query == ExtensionMethodParameterType.CLASS_CONFIG) {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter == ExtensionMethodParameterType.CLASS_CONFIG) {
                        argument = new ClassConfigImpl(pat.configureAnnotatedType());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                argumentsForAllInvocations.add(arguments);
            } else if (query == ExtensionMethodParameterType.METHOD_INFO) {
                for (jakarta.enterprise.inject.spi.AnnotatedMethod<?> targetMethod : pat.getAnnotatedType().getMethods()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_INFO) {
                            argument = new MethodInfoImpl(targetMethod);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
                for (jakarta.enterprise.inject.spi.AnnotatedConstructor<?> targetConstructor : pat.getAnnotatedType().getConstructors()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_INFO) {
                            argument = new MethodInfoImpl(targetConstructor);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.METHOD_CONFIG) {
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> targetMethodConfigurator : pat.configureAnnotatedType().methods()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_CONFIG) {
                            argument = new MethodConfigImpl(targetMethodConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator<?> targetConstructorConfigurator : pat.configureAnnotatedType().constructors()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_CONFIG) {
                            argument = new MethodConstructorConfigImpl(targetConstructorConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.FIELD_INFO) {
                for (jakarta.enterprise.inject.spi.AnnotatedField<?> targetField : pat.getAnnotatedType().getFields()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.FIELD_INFO) {
                            argument = new FieldInfoImpl(targetField);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.FIELD_CONFIG) {
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> targetFieldConfigurator : pat.configureAnnotatedType().fields()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.FIELD_CONFIG) {
                            argument = new FieldConfigImpl(targetFieldConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else {
                throw LiteExtensionTranslatorLogger.LOG.unknownQueryParameter(query);
            }

            for (List<Object> arguments : argumentsForAllInvocations) {
                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw LiteExtensionTranslatorLogger.LOG.unableToInvokeExtensionMethod(method, arguments, e.toString());
                }
            }
        };

        Enhancement enhancement = method.getAnnotation(Enhancement.class);
        actions.add(new ExtensionPhaseEnhancementAction(new HashSet<>(Arrays.asList(enhancement.types())), enhancement.withSubtypes(),
                new HashSet<>(Arrays.asList(enhancement.withAnnotations())), patAcceptor));
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl();
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
