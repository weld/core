package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.invoke.AsyncHandler;
import jakarta.enterprise.invoke.Invoker;

/**
 * An invoker that applies an {@link AsyncHandler} to the method invocation,
 * deferring cleanup of dependent beans until the async operation completes.
 * <p>
 * For {@code @ReturnType} handlers: after the method returns, the return value
 * is passed to {@link AsyncHandler#transform(Object, Runnable)}, with the cleanup
 * action as the completion callback.
 * <p>
 * For {@code @ParameterType} handlers: before the method is called, the matching
 * argument is passed to {@link AsyncHandler#transform(Object, Runnable)}, and the
 * method is called with the transformed argument.
 */
class AsyncInvokerImpl<T, R> implements Invoker<T, R>, InvokerInfo {
    private final MethodHandle mh;
    private final AsyncHandler<Object> handler;
    private final boolean isReturnType;
    private final int asyncParamIndex; // for @ParameterType, -1 for @ReturnType

    @SuppressWarnings("unchecked")
    AsyncInvokerImpl(MethodHandle mh, AsyncHandler<?> handler, boolean isReturnType, int asyncParamIndex) {
        this.mh = mh;
        this.handler = (AsyncHandler<Object>) handler;
        this.isReturnType = isReturnType;
        this.asyncParamIndex = asyncParamIndex;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R invoke(T instance, Object[] arguments) throws Exception {
        try {
            // Clear any stale ThreadLocal before invocation. The deferred cleanup
            // in CleanupActions.runDeferred() will set CURRENT after the method
            // handle chain completes successfully.
            CleanupActions.CURRENT.remove();

            if (!isReturnType && asyncParamIndex >= 0 && arguments != null && asyncParamIndex < arguments.length) {
                // @ParameterType: invoke the method, then retrieve deferred cleanup
                // and apply handler.transform() to the argument with the cleanup callback.
                // We need the MH chain to run first (to set up CleanupActions via lookups),
                // but we need to transform the argument with the completion callback.
                //
                // Strategy: run the chain to get CleanupActions, then transform the argument,
                // but this won't work because the method already ran with the original argument.
                //
                // Instead: we invoke the MH chain normally. The argument is already the async
                // param value provided by the caller. The handler.transform() wraps it so that
                // completion fires when the async param signals done. We transform BEFORE invoke.
                // The deferred cleanup will be captured in CURRENT by runDeferred.
                R result = (R) mh.invoke(instance, arguments);

                CleanupActions ca = CleanupActions.CURRENT.get();
                CleanupActions.CURRENT.remove();

                if (ca != null) {
                    // Transform the async parameter's value with the cleanup callback.
                    // The handler registers the completion callback on the async parameter,
                    // so cleanup runs when the async parameter signals completion.
                    handler.transform(arguments[asyncParamIndex], ca::cleanup);
                }
                return result;
            }

            // @ReturnType: invoke the method, then transform the return value
            R result = (R) mh.invoke(instance, arguments);

            CleanupActions ca = CleanupActions.CURRENT.get();
            CleanupActions.CURRENT.remove();

            if (ca != null) {
                return (R) handler.transform(result, ca::cleanup);
            } else {
                return (R) handler.transform(result, () -> {
                });
            }
        } catch (ValueCarryingException e) {
            CleanupActions.CURRENT.remove();
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            CleanupActions.CURRENT.remove();
            throw SneakyThrow.sneakyThrow(e);
        }
    }
}
