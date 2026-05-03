package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;

import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.invoke.AsyncHandler;
import jakarta.enterprise.invoke.Invoker;

/**
 * An invoker that applies an {@link AsyncHandler} to the method invocation,
 * deferring cleanup of dependent beans until the async operation completes.
 * <p>
 * For {@link AsyncHandler.ReturnType} handlers: after the method returns, the return value
 * is passed to {@link AsyncHandler.ReturnType#transform(Object, Runnable)}, with the cleanup
 * action as the completion callback.
 * <p>
 * For {@link AsyncHandler.ParameterType} handlers: before the method is called, the matching
 * argument is passed to {@link AsyncHandler.ParameterType#transformArgument(Object, Runnable)},
 * and after the method returns, the return value is passed to
 * {@link AsyncHandler.ParameterType#transformReturnValue(Object, Runnable)}.
 */
class AsyncInvokerImpl<T, R> implements Invoker<T, R>, InvokerInfo {
    private final MethodHandle mh;
    private final AsyncHandler.ReturnType<Object> returnTypeHandler;
    private final AsyncHandler.ParameterType<Object> parameterTypeHandler;
    private final int asyncParamIndex; // for ParameterType, -1 for ReturnType

    AsyncInvokerImpl(MethodHandle mh, AsyncHandlerRegistry.HandlerInfo handlerInfo, int asyncParamIndex) {
        this.mh = mh;
        if (handlerInfo.isReturnType()) {
            this.returnTypeHandler = handlerInfo.getReturnTypeHandler();
            this.parameterTypeHandler = null;
            this.asyncParamIndex = -1;
        } else {
            this.returnTypeHandler = null;
            this.parameterTypeHandler = handlerInfo.getParameterTypeHandler();
            this.asyncParamIndex = asyncParamIndex;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public R invoke(T instance, Object[] arguments) throws Exception {
        try {
            // Clear any stale ThreadLocal before invocation
            CleanupActions.CURRENT.remove();

            if (parameterTypeHandler != null && asyncParamIndex >= 0
                    && arguments != null && asyncParamIndex < arguments.length) {
                // ParameterType: invoke the method, then retrieve deferred cleanup
                R result = (R) mh.invoke(instance, arguments);

                CleanupActions ca = CleanupActions.CURRENT.get();
                CleanupActions.CURRENT.remove();

                if (ca != null) {
                    parameterTypeHandler.transformArgument(arguments[asyncParamIndex], ca::cleanup);
                    return (R) parameterTypeHandler.transformReturnValue(result, () -> {
                    });
                }
                return result;
            }

            // ReturnType: invoke the method, then transform the return value
            R result = (R) mh.invoke(instance, arguments);

            CleanupActions ca = CleanupActions.CURRENT.get();
            CleanupActions.CURRENT.remove();

            if (ca != null) {
                return (R) returnTypeHandler.transform(result, ca::cleanup);
            } else {
                return (R) returnTypeHandler.transform(result, () -> {
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
