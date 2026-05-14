package org.jboss.weld.invokable;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicInteger;

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
 * the completion callback before the method runs.
 * <p>
 * Cleanup of dependent beans is deferred via {@link DeferredCleanup} so that
 * it only happens once both the method has returned and the completion callback
 * has been called. See {@link DeferredCleanup} for details.
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
        DeferredCleanup deferred;
        if (requiresCleanup) {
            ca = new CleanupActions();
            deferred = new DeferredCleanup(ca);
            completion = deferred;
        } else {
            ca = null;
            deferred = null;
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
            if (deferred != null) {
                deferred.methodReturned();
            }
            // completion is still valid here; the state machine handles repeated calls
            return (R) parameterTypeHandler.transformReturnValue(result, completion);
        } catch (ValueCarryingException e) {
            if (deferred != null) {
                deferred.forceCleanup();
            }
            return (R) e.getMethodReturnValue();
        } catch (Throwable e) {
            if (deferred != null) {
                deferred.forceCleanup();
            }
            throw SneakyThrow.sneakyThrow(e);
        }
    }

    /**
     * Coordinates cleanup of dependent beans between the completion callback
     * (fired by the async handler) and the method return.
     * <p>
     * Cleanup must only run when <em>both</em> the completion callback has been
     * called and the target method has returned. This prevents premature destruction
     * of dependent beans when the completion callback fires synchronously during the
     * method body (e.g., the method calls {@code asyncParam.resume()} directly).
     * <p>
     * Uses a four-state machine driven by {@link AtomicInteger} CAS operations:
     *
     * <pre>
     *   PENDING (0)  ──run()──►  COMPLETION_SIGNALED (1)
     *       │                             │
     *   methodReturned()              methodReturned()
     *       │                             │
     *       ▼                             ▼
     *   METHOD_RETURNED (2)  ──run()──►  DONE (3) → cleanup runs
     * </pre>
     * <ul>
     * <li><b>Sync case</b> (completion fires during method): transitions
     * 0→1, then 1→3 on method return; cleanup runs after the method.</li>
     * <li><b>Async case</b> (completion fires after method): transitions
     * 0→2 on method return, then 2→3 on completion; cleanup runs on completion.</li>
     * <li><b>Exception case</b>: {@link #forceCleanup()} sets state to 3 directly.
     * The MH chain's {@code tryFinally/runExceptionOnly} may also call
     * {@code ca.cleanup()}, but that is safe because {@link CleanupActions#cleanup()}
     * clears its internal lists on first call.</li>
     * </ul>
     * Thread safety is ensured by CAS; the async completion callback may fire
     * on a different thread than the invoker.
     */
    private static class DeferredCleanup implements Runnable {
        private static final int PENDING = 0;
        private static final int COMPLETION_SIGNALED = 1;
        private static final int METHOD_RETURNED = 2;
        private static final int DONE = 3;

        private final CleanupActions ca;
        private final AtomicInteger state = new AtomicInteger(PENDING);

        DeferredCleanup(CleanupActions ca) {
            this.ca = ca;
        }

        @Override
        public void run() {
            if (state.compareAndSet(PENDING, COMPLETION_SIGNALED)) {
                return;
            }
            if (state.compareAndSet(METHOD_RETURNED, DONE)) {
                ca.cleanup();
            }
        }

        void methodReturned() {
            if (state.compareAndSet(PENDING, METHOD_RETURNED)) {
                return;
            }
            if (state.compareAndSet(COMPLETION_SIGNALED, DONE)) {
                ca.cleanup();
            }
        }

        void forceCleanup() {
            if (state.getAndSet(DONE) != DONE) {
                ca.cleanup();
            }
        }
    }
}
