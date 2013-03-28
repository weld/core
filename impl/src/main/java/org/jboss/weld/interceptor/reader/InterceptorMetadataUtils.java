package org.jboss.weld.interceptor.reader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.interceptor.builder.MethodReference;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.interceptor.util.InterceptorMetadataException;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.security.SetAccessibleAction;
import org.slf4j.cal10n.LocLogger;

import static org.jboss.weld.logging.Category.REFLECTION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.util.collections.WeldCollections.immutableMap;

/**
 * @author Marius Bogoevici
 */
public class InterceptorMetadataUtils {
    protected static final String OBJECT_CLASS_NAME = Object.class.getName();

    private static final LocLogger LOG = loggerFactory().getLogger(REFLECTION);


    public static InterceptorMetadata readMetadataForInterceptorClass(InterceptorFactory<?> interceptorReference) {
        return new DefaultInterceptorMetadata(interceptorReference, buildMethodMap(interceptorReference.getClassMetadata(), false));
    }

    public static <T> TargetClassInterceptorMetadata readMetadataForTargetClass(ClassMetadata<T> classMetadata) {
        return new TargetClassInterceptorMetadata(classMetadata, buildMethodMap(classMetadata, true));
    }

    public static boolean isInterceptorMethod(InterceptionType interceptionType, MethodMetadata method, boolean forTargetClass) {
        if (!method.getSupportedInterceptionTypes().contains(interceptionType)) {
            return false;
        }

        if (interceptionType.isLifecycleCallback()) {
            if (forTargetClass) {
                return isValidTargetClassLifecycleInterceptorMethod(interceptionType, method);
            } else {
                return isValidInterceptorClassLifecycleInterceptorMethod(interceptionType, method);
            }
        } else {
            return isValidBusinessMethodInterceptorMethod(interceptionType, method);
        }
    }

    private static boolean isValidTargetClassLifecycleInterceptorMethod(InterceptionType interceptionType, MethodMetadata method) {
        Method javaMethod = method.getJavaMethod();
        if (interceptionType == InterceptionType.AROUND_CONSTRUCT) {
            throw new DefinitionException(ValidatorMessage.AROUND_CONSTRUCT_INTERCEPTOR_METHOD_NOT_ALLOWED_ON_TARGET_CLASS,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName());
        }
        if (!Void.TYPE.equals(method.getReturnType())) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_VOID_RETURN_TYPE,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Void.TYPE.getName());
        }
        Class<?>[] parameterTypes = javaMethod.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_ZERO_PARAMETERS,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName());
        }
        Class<?>[] exceptionTypes = javaMethod.getExceptionTypes();
        if (exceptionTypes.length != 0) {
            for (Class<?> exceptionType : exceptionTypes) {
                if (!RuntimeException.class.isAssignableFrom(exceptionType)) {
                    LOG.warn(ValidatorMessage.INTERCEPTOR_METHOD_SHOULD_NOT_THROW_CHECKED_EXCEPTIONS,
                            javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                            exceptionType.getName());
                }
            }
        }

        return parameterTypes.length == 0;
    }

    private static boolean isValidInterceptorClassLifecycleInterceptorMethod(InterceptionType interceptionType, MethodMetadata method) {
        Method javaMethod = method.getJavaMethod();
        if (!Object.class.equals(method.getReturnType()) && !Void.TYPE.equals(method.getReturnType())) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_RETURN_OBJECT_OR_VOID,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Void.TYPE.getName(), OBJECT_CLASS_NAME);
        }

        Class<?>[] parameterTypes = javaMethod.getParameterTypes();
        if (parameterTypes.length == 0) {
            return false;
        } else if (parameterTypes.length == 1) {
            if (InvocationContext.class.isAssignableFrom(parameterTypes[0])) {
                return true;
            }
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_CORRECT_TYPE_OF_PARAMETER,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), InvocationContext.class.getName());
        } else {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_EXACTLY_ONE_PARAMETER,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName());
        }
    }

    private static boolean isValidBusinessMethodInterceptorMethod(InterceptionType interceptionType, MethodMetadata method) {
        Method javaMethod = method.getJavaMethod();
        if (!Object.class.equals(method.getReturnType())) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_RETURN_OBJECT,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), OBJECT_CLASS_NAME);
        }

        Class<?>[] parameterTypes = javaMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_EXACTLY_ONE_PARAMETER,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName());
        }
        if (!InvocationContext.class.isAssignableFrom(parameterTypes[0])) {
            throw new DefinitionException(ValidatorMessage.INTERCEPTOR_METHOD_DOES_NOT_HAVE_CORRECT_TYPE_OF_PARAMETER,
                    javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), InvocationContext.class.getName());
        }
        return true;
    }

    static Map<InterceptionType, List<MethodMetadata>> buildMethodMap(ClassMetadata<?> interceptorClass, boolean forTargetClass) {
        Map<InterceptionType, List<MethodMetadata>> methodMap = new HashMap<InterceptionType, List<MethodMetadata>>();
        ClassMetadata<?> currentClass = interceptorClass;
        Set<MethodReference> foundMethods = new HashSet<MethodReference>();
        do {
            Set<InterceptionType> detectedInterceptorTypes = new HashSet<InterceptionType>();

            for (MethodMetadata method : currentClass.getDeclaredMethods()) {
                MethodReference methodReference = MethodReference.of(method, Modifier.isPrivate(method.getJavaMethod().getModifiers()));
                if (!foundMethods.contains(methodReference)) {
                    for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
                        if (isInterceptorMethod(interceptionType, method, forTargetClass)) {
                            if (methodMap.get(interceptionType) == null) {
                                methodMap.put(interceptionType, new LinkedList<MethodMetadata>());
                            }
                            if (detectedInterceptorTypes.contains(interceptionType)) {
                                throw new InterceptorMetadataException("Same interception type cannot be specified twice on the same class");
                            } else {
                                detectedInterceptorTypes.add(interceptionType);
                            }
                            // add method in the list - if it is there already, it means that it has been added by a subclass
                            // final methods are treated separately, as a final method cannot override another method nor be
                            // overridden
                            AccessController.doPrivileged(SetAccessibleAction.of(method.getJavaMethod()));
                            if (!foundMethods.contains(methodReference)) {
                                methodMap.get(interceptionType).add(0, method);
                            }
                        }
                    }
                    // the method reference must be added anyway - overridden methods are not taken into consideration
                    foundMethods.add(methodReference);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        while (currentClass != null && !OBJECT_CLASS_NAME.equals(currentClass.getJavaClass().getName()));
        return immutableMap(methodMap);
    }
}
