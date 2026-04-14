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
        registerBuiltinHandler(CompletionStage.class, new BuiltinCompletionStageHandler(), true);
        registerBuiltinHandler(CompletableFuture.class, new BuiltinCompletableFutureHandler(), true);
        registerBuiltinHandler(Flow.Publisher.class, new BuiltinFlowPublisherHandler(), true);
    }

    private void registerBuiltinHandler(Class<?> asyncType, AsyncHandler<?> handler, boolean isReturnType) {
        HandlerInfo info = new HandlerInfo(handler, asyncType, isReturnType);
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
        for (Metadata<AsyncHandler> metadata : ServiceLoader.load(AsyncHandler.class, resourceLoader)) {
            validateAndRegister(metadata.getValue());
        }
    }

    private void validateAndRegister(AsyncHandler<?> handler) {
        Class<?> handlerClass = handler.getClass();

        // Must implement AsyncHandler directly (not through a subclass)
        validateDirectImplementation(handlerClass);

        // Extract async type from AsyncHandler<T>
        Class<?> asyncType = extractAsyncType(handlerClass);

        // Must be annotated with exactly one of @ReturnType or @ParameterType
        boolean isReturnType = handlerClass.isAnnotationPresent(AsyncHandler.ReturnType.class);
        boolean isParameterType = handlerClass.isAnnotationPresent(AsyncHandler.ParameterType.class);

        if (isReturnType && isParameterType) {
            throw InvokerLogger.LOG.asyncHandlerBothAnnotations(handlerClass);
        }
        if (!isReturnType && !isParameterType) {
            throw InvokerLogger.LOG.asyncHandlerNoAnnotation(handlerClass);
        }

        // Check for duplicate handlers for the same async type
        HandlerInfo existing = handlers.get(asyncType);
        if (existing != null && !existing.isBuiltin()) {
            throw InvokerLogger.LOG.asyncHandlerDuplicate(asyncType, handlerClass);
        }

        // Custom handlers override built-in handlers for the same type
        handlers.put(asyncType, new HandlerInfo(handler, asyncType, isReturnType));
    }

    private void validateDirectImplementation(Class<?> handlerClass) {
        // Must implement AsyncHandler directly, not through an abstract subclass
        boolean directlyImplements = false;
        for (Class<?> iface : handlerClass.getInterfaces()) {
            if (iface == AsyncHandler.class) {
                directlyImplements = true;
                break;
            }
        }
        if (!directlyImplements) {
            throw InvokerLogger.LOG.asyncHandlerIndirectImplementation(handlerClass);
        }
    }

    private Class<?> extractAsyncType(Class<?> handlerClass) {
        // Find AsyncHandler<T> in the direct superinterface types
        for (Type genericInterface : handlerClass.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                if (pt.getRawType() == AsyncHandler.class) {
                    Type typeArg = pt.getActualTypeArguments()[0];
                    return validateAndEraseAsyncType(typeArg, handlerClass);
                }
            } else if (genericInterface == AsyncHandler.class) {
                // Raw AsyncHandler — deployment problem
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
     * Finds a matching @ReturnType handler for the given method return type.
     */
    public HandlerInfo findReturnTypeHandler(Class<?> returnType) {
        HandlerInfo info = handlers.get(returnType);
        if (info != null && info.isReturnType()) {
            return info;
        }
        return null;
    }

    /**
     * Finds a matching @ParameterType handler for the given method parameter types.
     * Returns the handler info if exactly one parameter matches; null otherwise.
     */
    public HandlerInfo findParameterTypeHandler(Class<?>[] parameterTypes) {
        for (Class<?> paramType : parameterTypes) {
            HandlerInfo info = handlers.get(paramType);
            if (info != null && !info.isReturnType()) {
                return info;
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        handlers.clear();
    }

    /**
     * Holds information about a registered async handler.
     */
    public static class HandlerInfo {
        private final AsyncHandler<?> handler;
        private final Class<?> asyncType;
        private final boolean returnType;
        private boolean builtin;

        HandlerInfo(AsyncHandler<?> handler, Class<?> asyncType, boolean returnType) {
            this.handler = handler;
            this.asyncType = asyncType;
            this.returnType = returnType;
        }

        @SuppressWarnings("unchecked")
        public <T> AsyncHandler<T> getHandler() {
            return (AsyncHandler<T>) handler;
        }

        public Class<?> getAsyncType() {
            return asyncType;
        }

        public boolean isReturnType() {
            return returnType;
        }

        boolean isBuiltin() {
            return builtin;
        }

        void setBuiltin(boolean builtin) {
            this.builtin = builtin;
        }
    }

    // --- Built-in handlers ---

    @AsyncHandler.ReturnType
    static class BuiltinCompletionStageHandler<T> implements AsyncHandler<CompletionStage<T>> {
        @Override
        public CompletionStage<T> transform(CompletionStage<T> original, Runnable completion) {
            return original.whenComplete((value, error) -> completion.run());
        }
    }

    @AsyncHandler.ReturnType
    static class BuiltinCompletableFutureHandler<T> implements AsyncHandler<CompletableFuture<T>> {
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

    @AsyncHandler.ReturnType
    static class BuiltinFlowPublisherHandler<T> implements AsyncHandler<Flow.Publisher<T>> {
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
