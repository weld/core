package org.jboss.weld.bean.proxy;

import static org.jboss.weld.bean.proxy.InterceptionDecorationContext.endInterceptorContext;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A method handler that wraps the invocation of interceptors and decorators.
 *
 * @author Marius Bogoevici
 */
public class CombinedInterceptorAndDecoratorStackMethodHandler implements MethodHandler, Serializable {

    public static final CombinedInterceptorAndDecoratorStackMethodHandler NULL_INSTANCE = new CombinedInterceptorAndDecoratorStackMethodHandler() {
        @Override
        public void setInterceptorMethodHandler(MethodHandler interceptorMethodHandler) {
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

    private MethodHandler interceptorMethodHandler;

    private Object outerDecorator;

    public void setInterceptorMethodHandler(MethodHandler interceptorMethodHandler) {
        this.interceptorMethodHandler = interceptorMethodHandler;
    }

    public void setOuterDecorator(Object outerDecorator) {
        this.outerDecorator = outerDecorator;
    }

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {

        if (InterceptionDecorationContext.startIfNotOnTop(this)) {
            try {
                if (interceptorMethodHandler != null) {
                    if (proceed != null) {
                        if (outerDecorator == null) {
                            // use WeldSubclass.method$$super() as proceed
                            return this.interceptorMethodHandler.invoke(self, thisMethod, proceed, args);
                        } else {
                            return this.interceptorMethodHandler.invoke(outerDecorator, thisMethod, thisMethod, args);
                        }
                    } else {
                        return this.interceptorMethodHandler.invoke(self, thisMethod, null, args);
                    }
                } else {
                    if (outerDecorator != null) {
                        SecurityActions.ensureAccessible(thisMethod);
                        return Reflections.invokeAndUnwrap(outerDecorator, thisMethod, args);
                    }
                }
            } finally {
                endInterceptorContext();
            }
        }
        SecurityActions.ensureAccessible(proceed);
        return Reflections.invokeAndUnwrap(self, proceed, args);
    }

    public MethodHandler getInterceptorMethodHandler() {
        return interceptorMethodHandler;
    }

    public Object getOuterDecorator() {
        return outerDecorator;
    }

    public boolean isDisabledHandler() {
        return this == InterceptionDecorationContext.peekIfNotEmpty();
    }
}
