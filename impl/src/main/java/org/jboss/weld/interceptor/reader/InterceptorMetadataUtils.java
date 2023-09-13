package org.jboss.weld.interceptor.reader;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.interceptor.InvocationContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author Marius Bogoevici
 * @author Marko Luksa
 */
public class InterceptorMetadataUtils {
    protected static final String OBJECT_CLASS_NAME = Object.class.getName();

    private InterceptorMetadataUtils() {
    }

    public static boolean isInterceptorMethod(InterceptionType interceptionType, Method method, boolean forTargetClass) {
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

    private static boolean isValidTargetClassLifecycleInterceptorMethod(InterceptionType interceptionType, Method method) {
        /*
         * This check is relaxed (WELD-1399) because we are not able to distinguish a CDI managed bean from
         * an interceptor class bound using @Interceptors.
         *
         * This will be revisited as part of https://issues.jboss.org/browse/WELD-1401
         *
         * if (interceptionType == InterceptionType.AROUND_CONSTRUCT) {
         * throw new DefinitionException(ValidatorMessage.AROUND_CONSTRUCT_INTERCEPTOR_METHOD_NOT_ALLOWED_ON_TARGET_CLASS,
         * javaMethod.getName(), javaMethod.getDeclaringClass().getName(),
         * interceptionType.annotationClassName());
         * }
         */
        /*
         * Again, we relax the check and allow both void and Object return types as we cannot distinguish between
         * a managed bean and an interceptor class.
         */
        if (!Void.TYPE.equals(method.getReturnType()) && !Object.class.equals(method.getReturnType())) {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotHaveVoidReturnType(
                    method.getName(), method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Void.TYPE.getName(), Formats.formatAsStackTraceElement(method));
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            ValidatorLogger.LOG.interceptorMethodDoesNotHaveZeroParameters(
                    method.getName(), method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName());
        }
        if (parameterTypes.length > 1) {
            throw ValidatorLogger.LOG.interceptorMethodDeclaresMultipleParameters(
                    method.getName(), method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Formats.formatAsStackTraceElement(method));
        }
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length != 0) {
            for (Class<?> exceptionType : exceptionTypes) {
                if (!RuntimeException.class.isAssignableFrom(exceptionType)) {
                    ValidatorLogger.LOG.interceptorMethodShouldNotThrowCheckedExceptions(
                            method.getName(), method.getDeclaringClass().getName(),
                            exceptionType.getName(), Formats.formatAsStackTraceElement(method));
                }
            }
        }

        return parameterTypes.length == 0;
    }

    private static boolean isValidInterceptorClassLifecycleInterceptorMethod(InterceptionType interceptionType, Method method) {
        if (!Object.class.equals(method.getReturnType()) && !Void.TYPE.equals(method.getReturnType())) {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotReturnObjectOrVoid(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Void.TYPE.getName(), OBJECT_CLASS_NAME,
                    Formats.formatAsStackTraceElement(method));
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return false;
        } else if (parameterTypes.length == 1) {
            if (InvocationContext.class.isAssignableFrom(parameterTypes[0])) {
                return true;
            }
            throw ValidatorLogger.LOG.interceptorMethodDoesNotHaveCorrectTypeOfParameter(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), InvocationContext.class.getName(),
                    Formats.formatAsStackTraceElement(method));
        } else {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotHaveExactlyOneParameter(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Formats.formatAsStackTraceElement(method));
        }
    }

    private static boolean isValidBusinessMethodInterceptorMethod(InterceptionType interceptionType, Method method) {
        if (!Object.class.equals(method.getReturnType())) {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotReturnObject(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), OBJECT_CLASS_NAME, Formats.formatAsStackTraceElement(method));
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotHaveExactlyOneParameter(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), Formats.formatAsStackTraceElement(method));
        }
        if (!InvocationContext.class.isAssignableFrom(parameterTypes[0])) {
            throw ValidatorLogger.LOG.interceptorMethodDoesNotHaveCorrectTypeOfParameter(method.getName(),
                    method.getDeclaringClass().getName(),
                    interceptionType.annotationClassName(), InvocationContext.class.getName(),
                    Formats.formatAsStackTraceElement(method));
        }
        return true;
    }

    public static Map<InterceptionType, List<Method>> buildMethodMap(EnhancedAnnotatedType<?> type, boolean forTargetClass,
            BeanManagerImpl manager) {
        ImmutableMap.Builder<InterceptionType, List<Method>> builder = null;
        for (InterceptionType interceptionType : InterceptionTypeRegistry.getSupportedInterceptionTypes()) {
            List<Method> value = BeanMethods.getInterceptorMethods(type, interceptionType, forTargetClass);
            if (!value.isEmpty()) {
                if (builder == null) {
                    builder = ImmutableMap.builder();
                }
                builder.put(interceptionType, value);
            }
        }
        if (builder == null) {
            return Collections.emptyMap();
        }
        return builder.build();
    }
}
