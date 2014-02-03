package org.jboss.weld.bean.proxy;

import static org.jboss.weld.bean.proxy.InterceptionDecorationContext.endInterceptorContext;
import static org.jboss.weld.bean.proxy.InterceptionDecorationContext.startInterceptorContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A method handler that wraps the invocation of interceptors and decorators.
 *
 * @author Marius Bogoevici
 */
public class CombinedInterceptorAndDecoratorStackMethodHandler implements MethodHandler, Serializable {

    private MethodHandler interceptorMethodHandler;

    private Object outerDecorator;


    public void setInterceptorMethodHandler(MethodHandler interceptorMethodHandler) {
        this.interceptorMethodHandler = interceptorMethodHandler;
    }

    public void setOuterDecorator(Object outerDecorator) {
        this.outerDecorator = outerDecorator;
    }

    private Set<CombinedInterceptorAndDecoratorStackMethodHandler> getDisabledHandlers() {
        return InterceptionDecorationContext.peek();
    }

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        boolean externalContext = false;

        try {
            if (InterceptionDecorationContext.empty()) {
                externalContext = true;
                startInterceptorContext();
            }
            Set<CombinedInterceptorAndDecoratorStackMethodHandler> disabledHandlers = getDisabledHandlers();
            if (!disabledHandlers.contains(this)) {
                try {

                    disabledHandlers.add(this);
                    if (interceptorMethodHandler != null) {
                        if (proceed != null) {
                            return this.interceptorMethodHandler.invoke(outerDecorator != null ? outerDecorator : self, thisMethod, thisMethod, args);
                        } else {
                            return this.interceptorMethodHandler.invoke(self, thisMethod, null, args);
                        }
                    } else {
                        if (outerDecorator != null) {
                            SetAccessibleAction.ensureAccessible(thisMethod);
                            return Reflections.invokeAndUnwrap(outerDecorator, thisMethod, args);
                        }
                    }
                } finally {
                    disabledHandlers.remove(this);
                }
            }
            SetAccessibleAction.ensureAccessible(proceed);
            return Reflections.invokeAndUnwrap(self, proceed, args);
        } finally {
            if (externalContext) {
                endInterceptorContext();
            }
        }
    }

    public boolean isDisabledHandler() {
        if (InterceptionDecorationContext.empty()) {
            return false;
        }
        return getDisabledHandlers().contains(this);
    }

    public MethodHandler getInterceptorMethodHandler() {
        return interceptorMethodHandler;
    }

    public Object getOuterDecorator() {
        return outerDecorator;
    }
}
