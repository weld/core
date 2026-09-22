package org.jboss.weld.invokable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    private final Map<Class<?>, Map<Class<?>, HandlerInfo>> candidates = new HashMap<>();
    private final Map<String, String> selections = new HashMap<>();

    /**
     * Creates a new registry with built-in handlers pre-registered.
     */
    public AsyncHandlerRegistry() {
        this("");
    }

    public AsyncHandlerRegistry(String configuration) {
        if (!configuration.isBlank()) {
            for (String entry : configuration.split(",", -1)) {
                String[] mapping = entry.split("=", -1);
                if (mapping.length != 2 || mapping[0].isBlank() || mapping[1].isBlank()
                        || selections.putIfAbsent(mapping[0].trim(), mapping[1].trim()) != null) {
                    throw InvokerLogger.LOG.invalidAsyncHandlerConfiguration(configuration);
                }
            }
        }
        // Built-in handlers required by the spec
        registerBuiltinReturnTypeHandler(CompletionStage.class, new BuiltinCompletionStageHandler());
        registerBuiltinReturnTypeHandler(CompletableFuture.class, new BuiltinCompletableFutureHandler());
        registerBuiltinReturnTypeHandler(Flow.Publisher.class, new BuiltinFlowPublisherHandler());
    }

    private void registerBuiltinReturnTypeHandler(Class<?> asyncType, AsyncHandler.ReturnType<?> handler) {
        HandlerInfo info = HandlerInfo.returnType(handler, asyncType);
        handlers.put(asyncType, info);
        register(info);
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
        register(HandlerInfo.returnType(handler, asyncType));
    }

    private void validateAndRegisterParameterType(AsyncHandler.ParameterType<?> handler) {
        Class<?> handlerClass = handler.getClass();
        validateDirectImplementation(handlerClass, AsyncHandler.ParameterType.class);
        Class<?> asyncType = extractAsyncType(handlerClass, AsyncHandler.ParameterType.class);
        register(HandlerInfo.parameterType(handler, asyncType));
    }

    private void validateDirectImplementation(Class<?> handlerClass, Class<?> targetInterface) {
        if (AsyncHandler.ReturnType.class.isAssignableFrom(handlerClass)
                && AsyncHandler.ParameterType.class.isAssignableFrom(handlerClass)) {
            throw InvokerLogger.LOG.asyncHandlerBothKinds(handlerClass);
        }
        for (Class<?> iface : handlerClass.getInterfaces()) {
            if (iface == targetInterface) {
                return;
            }
        }
        throw InvokerLogger.LOG.asyncHandlerIndirectImplementation(handlerClass);
    }

    private void register(HandlerInfo info) {
        // Repeated discovery through BDAs sharing a classloader is not a duplicate provider.
        Map<Class<?>, HandlerInfo> providers = candidates.computeIfAbsent(info.getAsyncType(), key -> new HashMap<>());
        providers.putIfAbsent(info.getHandlerClass(), info);
    }

    /**
     * Resolves configured providers after discovery in every bean deployment archive has completed.
     */
    public void validateHandlers() {
        Map<Class<?>, HandlerInfo> resolved = new HashMap<>();
        Set<String> unresolved = new HashSet<>(selections.keySet());
        for (Map.Entry<Class<?>, Map<Class<?>, HandlerInfo>> entry : candidates.entrySet()) {
            Class<?> asyncType = entry.getKey();
            String selection = selections.get(asyncType.getName());
            HandlerInfo chosen = null;
            for (HandlerInfo candidate : entry.getValue().values()) {
                if (selection == null || candidate.getHandlerClass().getName().equals(selection)) {
                    if (chosen != null) {
                        throw InvokerLogger.LOG.asyncHandlerDuplicate(asyncType, entry.getValue().keySet());
                    }
                    chosen = candidate;
                }
            }
            if (chosen == null) {
                throw InvokerLogger.LOG.invalidAsyncHandlerSelection(asyncType.getName(), selection);
            }
            resolved.put(asyncType, chosen);
            unresolved.remove(asyncType.getName());
        }
        if (!unresolved.isEmpty()) {
            String asyncType = unresolved.iterator().next();
            throw InvokerLogger.LOG.invalidAsyncHandlerSelection(asyncType, selections.get(asyncType));
        }
        handlers.clear();
        handlers.putAll(resolved);
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
    private HandlerInfo findReturnTypeHandler(Class<?> returnType) {
        HandlerInfo info = handlers.get(returnType);
        if (info != null && info.isReturnType()) {
            return info;
        }
        return null;
    }

    /**
     * Selects a handler only when exactly one return or parameter type handler matches.
     */
    public HandlerInfo findHandler(Class<?> returnType, Class<?>[] parameterTypes) {
        return selectHandler(findReturnTypeHandler(returnType), parameterTypes);
    }

    private HandlerInfo selectHandler(HandlerInfo match, Class<?>[] parameterTypes) {
        Map<Class<?>, Integer> occurrences = new HashMap<>();
        for (Class<?> paramType : parameterTypes) {
            occurrences.merge(paramType, 1, Integer::sum);
        }
        for (Map.Entry<Class<?>, Integer> entry : occurrences.entrySet()) {
            HandlerInfo info = handlers.get(entry.getKey());
            if (entry.getValue() == 1 && info != null && !info.isReturnType()) {
                if (match != null) {
                    return null;
                }
                match = info;
            }
        }
        return match;
    }

    @Override
    public void cleanup() {
        handlers.clear();
        candidates.clear();
        selections.clear();
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
