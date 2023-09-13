package org.jboss.weld.bean.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.interceptor.proxy.InterceptorMethodHandler;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A method handler that wraps the invocation of interceptors and decorators.
 *
 * @author Marius Bogoevici
 */
public class CombinedInterceptorAndDecoratorStackMethodHandler implements StackAwareMethodHandler, Serializable {

    public static final CombinedInterceptorAndDecoratorStackMethodHandler NULL_INSTANCE = new CombinedInterceptorAndDecoratorStackMethodHandler() {
        @Override
        public void setInterceptorMethodHandler(InterceptorMethodHandler interceptorMethodHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOuterDecorator(Object outerDecorator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            throw new UnsupportedOperationException();
        }
    };

    private InterceptorMethodHandler interceptorMethodHandler;

    private Object outerDecorator;

    public void setInterceptorMethodHandler(InterceptorMethodHandler interceptorMethodHandler) {
        this.interceptorMethodHandler = interceptorMethodHandler;
    }

    public void setOuterDecorator(Object outerDecorator) {
        this.outerDecorator = outerDecorator;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke(Stack stack, Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (stack == null) {
            /*
             * This is a lifecycle callback invocation.
             * 1) Lifecycle callback interception is never suppressed by IDC
             * 2) Interception of methods called by lifecycle callbacks is suppressed
             */
            stack = InterceptionDecorationContext.getStack();
            return invoke(stack, self, thisMethod, proceed, args, true, stack.startIfNotOnTop(this));
        } else {
            /*
             * This is an around-invoke interception
             * Interceptors are invoked as long as the current IDC is not suppressed.
             */
            boolean intercept = stack.startIfNotOnTop(this);
            return invoke(stack, self, thisMethod, proceed, args, intercept, intercept);
        }
    }

    public Object invoke(Stack stack, Object self, Method thisMethod, Method proceed, Object[] args, boolean intercept,
            boolean popStack) throws Throwable {
        if (intercept) {
            try {
                if (interceptorMethodHandler != null) {
                    if (proceed != null) {
                        if (outerDecorator == null) {
                            // use WeldSubclass.method$$super() as proceed
                            return this.interceptorMethodHandler.invoke(stack, self, thisMethod, proceed, args);
                        } else {
                            return this.interceptorMethodHandler.invoke(stack, outerDecorator, thisMethod, thisMethod, args);
                        }
                    } else {
                        return this.interceptorMethodHandler.invoke(stack, self, thisMethod, null, args);
                    }
                } else {
                    if (outerDecorator != null) {
                        SecurityActions.ensureAccessible(thisMethod);
                        return Reflections.invokeAndUnwrap(outerDecorator, thisMethod, args);
                    }
                }
            } finally {
                if (popStack) {
                    stack.end();
                }
            }
        }
        SecurityActions.ensureAccessible(proceed);
        return Reflections.invokeAndUnwrap(self, proceed, args);
    }

    public InterceptorMethodHandler getInterceptorMethodHandler() {
        return interceptorMethodHandler;
    }

    public Object getOuterDecorator() {
        return outerDecorator;
    }

    public boolean isDisabledHandler() {
        return this == InterceptionDecorationContext.peekIfNotEmpty();
    }

    public boolean isDisabledHandler(Stack stack) {
        return this == stack.peek();
    }
}
