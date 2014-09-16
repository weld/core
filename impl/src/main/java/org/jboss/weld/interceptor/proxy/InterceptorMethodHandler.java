package org.jboss.weld.interceptor.proxy;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionUtils;

/**
 * @author Marius Bogoevici
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class InterceptorMethodHandler implements MethodHandler, Serializable {

    public static final String INTERCEPTOR_BINDINGS_KEY = "org.jboss.weld.interceptor.bindings";

    private final InterceptionContext ctx;

    public InterceptorMethodHandler(InterceptionContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        SecurityActions.ensureAccessible(thisMethod);
        if (proceed == null) {
            if (thisMethod.getName().equals(InterceptionUtils.POST_CONSTRUCT)) {
                return executeInterception(self, null, null, InterceptionType.POST_CONSTRUCT);
            } else if (thisMethod.getName().equals(InterceptionUtils.PRE_DESTROY)) {
                return executeInterception(self, null, null, InterceptionType.PRE_DESTROY);
            }
        } else {
            if (isInterceptorMethod(thisMethod)) {
                return proceed.invoke(self, args);
            }
            return executeInterception(self, thisMethod, args, InterceptionType.AROUND_INVOKE);
        }
        return null;
    }

    protected Object executeInterception(Object instance, Method method, Object[] args, InterceptionType interceptionType) throws Throwable {
        SimpleInterceptionChain chain = new SimpleInterceptionChain(instance, method, args, interceptionType, ctx);

        // WELD-1742 Associate method interceptor bindings
        Set<Annotation> interceptorBindings = null;
        switch (interceptionType) {
            case AROUND_INVOKE:
            case AROUND_TIMEOUT:
                interceptorBindings = ctx.getInterceptionModel().getMemberInterceptorBindings(method);
                break;
            case POST_CONSTRUCT:
            case PRE_DESTROY:
                interceptorBindings = ctx.getInterceptionModel().getClassInterceptorBindings();
                break;
            default:
                throw new IllegalStateException("Invalid interception type");
        }
        InterceptorInvocationContext invocationContext = new InterceptorInvocationContext(chain, instance, method, args, interceptorBindings);
        invocationContext.getContextData().put(INTERCEPTOR_BINDINGS_KEY, interceptorBindings);

        return chain.invokeNextInterceptor(invocationContext);
    }

    private boolean isInterceptorMethod(Method method) {
        return ctx.getInterceptionModel().getTargetClassInterceptorMetadata().isInterceptorMethod(method);
    }
}
