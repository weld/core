package org.jboss.weld.invokable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import jakarta.enterprise.invoke.AsyncHandler;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.logging.InvokerLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ServiceLoader;

/**
 * Discovers, validates, and stores {@link AsyncHandler} implementations.
 * <p>
 * Async handlers are discovered via {@link ServiceLoader} and validated during deployment.
 * Built-in handlers for {@link CompletionStage}, {@link CompletableFuture},
 * and {@link Flow.Publisher} are always provided.
 */
public class AsyncHandlerRegistry implements Service {

    // Maps async type (erasure) to handler info
    private final Map<Class<?>, HandlerInfo> handlers = new HashMap<>();

    /**
     * Creates a new registry with built-in handlers pre-registered.
     */
    public AsyncHandlerRegistry() {
        // Built-in handlers required by the spec
        registerBuiltinReturnTypeHandler(CompletionStage.class, new BuiltinCompletionStageHandler());
        registerBuiltinReturnTypeHandler(CompletableFuture.class, new BuiltinCompletableFutureHandler());
        registerBuiltinReturnTypeHandler(Flow.Publisher.class, new BuiltinFlowPublisherHandler());
    }

    private void registerBuiltinReturnTypeHandler(Class<?> asyncType, AsyncHandler.ReturnType<?> handler) {
        HandlerInfo info = HandlerInfo.returnType(handler, asyncType);
        info.setBuiltin(true);
        handlers.put(asyncType, info);
    }

    /**
     * Discovers and validates async handlers using the given resource loader.
     * Called per BDA during deployment.
     *
     * @param resourceLoader the resource loader for discovering services
     */
    public void discoverHandlers(ResourceLoader resourceLoader) {
        if (resourceLoader == null) {
            return;
        }
        for (Metadata<AsyncHandler.ReturnType> metadata : ServiceLoader.load(AsyncHandler.ReturnType.class, resourceLoader)) {
            validateAndRegisterReturnType(metadata.getValue());
        }
        for (Metadata<AsyncHandler.ParameterType> metadata : ServiceLoader.load(AsyncHandler.ParameterType.class,
                resourceLoader)) {
            validateAndRegisterParameterType(metadata.getValue());
        }
    }

    private void validateAndRegisterReturnType(AsyncHandler.ReturnType<?> handler) {
        Class<?> handlerClass = handler.getClass();
        validateDirectImplementation(handlerClass, AsyncHandler.ReturnType.class);
        Class<?> asyncType = extractAsyncType(handlerClass, AsyncHandler.ReturnType.class);
        checkDuplicate(asyncType, handlerClass, true);
        handlers.put(asyncType, HandlerInfo.returnType(handler, asyncType));
    }

    private void validateAndRegisterParameterType(AsyncHandler.ParameterType<?> handler) {
        Class<?> handlerClass = handler.getClass();
        validateDirectImplementation(handlerClass, AsyncHandler.ParameterType.class);
        Class<?> asyncType = extractAsyncType(handlerClass, AsyncHandler.ParameterType.class);
        checkDuplicate(asyncType, handlerClass, false);
        handlers.put(asyncType, HandlerInfo.parameterType(handler, asyncType));
    }

    private void validateDirectImplementation(Class<?> handlerClass, Class<?> targetInterface) {
        for (Class<?> iface : handlerClass.getInterfaces()) {
            if (iface == targetInterface) {
                return;
            }
        }
        throw InvokerLogger.LOG.asyncHandlerIndirectImplementation(handlerClass);
    }

    private void checkDuplicate(Class<?> asyncType, Class<?> handlerClass, boolean isReturnType) {
        HandlerInfo existing = handlers.get(asyncType);
        if (existing != null && !existing.isBuiltin()) {
            if (existing.getHandlerClass() == handlerClass && existing.isReturnType() != isReturnType) {
                throw InvokerLogger.LOG.asyncHandlerBothKinds(handlerClass, asyncType);
            }
            throw InvokerLogger.LOG.asyncHandlerDuplicate(asyncType, handlerClass);
        }
    }

    private Class<?> extractAsyncType(Class<?> handlerClass, Class<?> targetInterface) {
        for (Type genericInterface : handlerClass.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                if (pt.getRawType() == targetInterface) {
                    Type typeArg = pt.getActualTypeArguments()[0];
                    return validateAndEraseAsyncType(typeArg, handlerClass);
                }
            } else if (genericInterface == targetInterface) {
                throw InvokerLogger.LOG.asyncHandlerRawType(handlerClass);
            }
        }
        // Should not happen if validateDirectImplementation passed
        throw InvokerLogger.LOG.asyncHandlerRawType(handlerClass);
    }

    private Class<?> validateAndEraseAsyncType(Type type, Class<?> handlerClass) {
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            if (cls.isArray()) {
                throw InvokerLogger.LOG.asyncHandlerArrayType(handlerClass);
            }
            return cls;
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        } else if (type instanceof TypeVariable) {
            throw InvokerLogger.LOG.asyncHandlerTypeVariable(handlerClass);
        }
        throw InvokerLogger.LOG.asyncHandlerRawType(handlerClass);
    }

    /**
     * Returns the handler info for the given async type, or null if none exists.
     */
    public HandlerInfo getHandler(Class<?> asyncType) {
        return handlers.get(asyncType);
    }

    /**
     * Returns true if a handler exists for the given async type.
     */
    public boolean hasHandler(Class<?> asyncType) {
        return handlers.containsKey(asyncType);
    }

    /**
     * Finds a matching ReturnType handler for the given method return type.
     */
    public HandlerInfo findReturnTypeHandler(Class<?> returnType) {
        HandlerInfo info = handlers.get(returnType);
        if (info != null && info.isReturnType()) {
            return info;
        }
        return null;
    }

    /**
     * Finds a matching ParameterType handler for the given method parameter types.
     * Returns the handler info if exactly one parameter matches; null otherwise.
     */
    public HandlerInfo findParameterTypeHandler(Class<?>[] parameterTypes) {
        HandlerInfo match = null;
        int matchCount = 0;
        for (Class<?> paramType : parameterTypes) {
            HandlerInfo info = handlers.get(paramType);
            if (info != null && !info.isReturnType()) {
                match = info;
                matchCount++;
            }
        }
        // spec requires exactly one matching parameter; with 0 or 2+ matches
        // the method is not considered async and null signals synchronous cleanup
        return matchCount == 1 ? match : null;
    }

    @Override
    public void cleanup() {
        handlers.clear();
    }

    /**
     * Holds information about a registered async handler.
     */
    public static class HandlerInfo {
        private final AsyncHandler.ReturnType<?> returnTypeHandler;
        private final AsyncHandler.ParameterType<?> parameterTypeHandler;
        private final Class<?> asyncType;
        private final Class<?> handlerClass;
        private final boolean isReturnType;
        private boolean builtin;

        static HandlerInfo returnType(AsyncHandler.ReturnType<?> handler, Class<?> asyncType) {
            return new HandlerInfo(handler, null, asyncType, handler.getClass(), true);
        }

        static HandlerInfo parameterType(AsyncHandler.ParameterType<?> handler, Class<?> asyncType) {
            return new HandlerInfo(null, handler, asyncType, handler.getClass(), false);
        }

        private HandlerInfo(AsyncHandler.ReturnType<?> returnTypeHandler,
                AsyncHandler.ParameterType<?> parameterTypeHandler,
                Class<?> asyncType, Class<?> handlerClass, boolean isReturnType) {
            this.returnTypeHandler = returnTypeHandler;
            this.parameterTypeHandler = parameterTypeHandler;
            this.asyncType = asyncType;
            this.handlerClass = handlerClass;
            this.isReturnType = isReturnType;
        }

        @SuppressWarnings("unchecked")
        public <T> AsyncHandler.ReturnType<T> getReturnTypeHandler() {
            return (AsyncHandler.ReturnType<T>) returnTypeHandler;
        }

        @SuppressWarnings("unchecked")
        public <T> AsyncHandler.ParameterType<T> getParameterTypeHandler() {
            return (AsyncHandler.ParameterType<T>) parameterTypeHandler;
        }

        public Class<?> getAsyncType() {
            return asyncType;
        }

        public Class<?> getHandlerClass() {
            return handlerClass;
        }

        public boolean isReturnType() {
            return isReturnType;
        }

        boolean isBuiltin() {
            return builtin;
        }

        void setBuiltin(boolean builtin) {
            this.builtin = builtin;
        }
    }

    // --- Built-in handlers ---

    static class BuiltinCompletionStageHandler<T> implements AsyncHandler.ReturnType<CompletionStage<T>> {
        @Override
        public CompletionStage<T> transform(CompletionStage<T> original, Runnable completion) {
            return original.whenComplete((value, error) -> completion.run());
        }
    }

    static class BuiltinCompletableFutureHandler<T> implements AsyncHandler.ReturnType<CompletableFuture<T>> {
        @Override
        public CompletableFuture<T> transform(CompletableFuture<T> original, Runnable completion) {
            CompletableFuture<T> result = new CompletableFuture<>();
            original.whenComplete((value, error) -> {
                completion.run();
                if (error != null) {
                    result.completeExceptionally(error);
                } else {
                    result.complete(value);
                }
            });
            return result;
        }
    }

    static class BuiltinFlowPublisherHandler<T> implements AsyncHandler.ReturnType<Flow.Publisher<T>> {
        @Override
        public Flow.Publisher<T> transform(Flow.Publisher<T> original, Runnable completion) {
            return subscriber -> original.subscribe(new Flow.Subscriber<T>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(T item) {
                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    completion.run();
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    completion.run();
                    subscriber.onComplete();
                }
            });
        }
    }
}
