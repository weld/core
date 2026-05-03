package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.invoke.AsyncHandler;
import jakarta.enterprise.invoke.Invoker;

/**
 * Invoker for methods matching an {@link AsyncHandler.ParameterType} handler.
 * <p>
 * Only used for ParameterType handlers. ReturnType handlers are embedded directly
 * into the method handle chain's {@code tryFinally} cleanup (see
 * {@link CleanupActions#runWithReturnTypeHandler}) and use plain {@link InvokerImpl}.
 * <p>
 * When the method handle chain requires cleanup (dependent bean lookups, transformers
 * with cleanup), the chain is built without internal {@link CleanupActions} creation
 * ({@code foldArguments} is skipped). Instead, this invoker creates
 * {@code CleanupActions} externally and passes it as the first parameter to
 * {@code mh.invoke()}. This allows {@code transformArgument} to receive
 * {@code ca::cleanup} as the completion callback before the method runs, which is
 * required for correct behavior when the async parameter completes synchronously
 * during the method body.
 * <p>
 * When no cleanup is needed, the chain has no {@code CleanupActions} at all and
 * the handler receives a no-op completion callback.
 */
class AsyncInvokerImpl<T, R> implements Invoker<T, R>, InvokerInfo {
    private final MethodHandle mh;
    private final AsyncHandler.ParameterType<Object> parameterTypeHandler;
    private final int asyncParamIndex;
    private final boolean requiresCleanup;

    AsyncInvokerImpl(MethodHandle mh, AsyncHandlerRegistry.HandlerInfo handlerInfo,
            int asyncParamIndex, boolean requiresCleanup) {
        this.mh = mh;
        this.parameterTypeHandler = handlerInfo.getParameterTypeHandler();
        this.asyncParamIndex = asyncParamIndex;
        this.requiresCleanup = requiresCleanup;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R invoke(T instance, Object[] arguments) throws Exception {
        Runnable completion;
        CleanupActions ca;
        if (requiresCleanup) {
            ca = new CleanupActions();
            completion = ca::cleanup;
        } else {
            ca = null;
            completion = () -> {
            };
        }

        arguments[asyncParamIndex] = parameterTypeHandler
                .transformArgument(arguments[asyncParamIndex], completion);

        try {
            R result;
            if (ca != null) {
                result = (R) mh.invoke(ca, instance, arguments);
            } else {
                result = (R) mh.invoke(instance, arguments);
            }
            return (R) parameterTypeHandler.transformReturnValue(result, completion);
        } catch (ValueCarryingException e) {
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            throw SneakyThrow.sneakyThrow(e);
        }
    }
}
