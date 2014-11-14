package org.jboss.weld.interceptor.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bean.proxy.InterceptionDecorationContext;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.bean.proxy.StackAwareMethodHandler;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author Marius Bogoevici
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class InterceptorMethodHandler implements StackAwareMethodHandler, Serializable {

    public static final String INTERCEPTOR_BINDINGS_KEY = "org.jboss.weld.interceptor.bindings";
    private static final long serialVersionUID = 1L;

    private final InterceptionContext ctx;
    private final transient ConcurrentMap<Method, CachedInterceptionChain> cachedChains;

    public InterceptorMethodHandler(InterceptionContext ctx) {
        this.ctx = ctx;
        this.cachedChains = new ConcurrentHashMap<Method, CachedInterceptionChain>();
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return invoke(InterceptionDecorationContext.getStack(), self, thisMethod, proceed, args);
    }

    public Object invoke(Stack stack, Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        SecurityActions.ensureAccessible(proceed);
        if (proceed == null) {
            if (thisMethod.getName().equals(InterceptionUtils.POST_CONSTRUCT)) {
                return executeInterception(self, null, null, null, InterceptionType.POST_CONSTRUCT, stack);
            } else if (thisMethod.getName().equals(InterceptionUtils.PRE_DESTROY)) {
                return executeInterception(self, null, null, null, InterceptionType.PRE_DESTROY, stack);
            }
        } else {
            if (isInterceptorMethod(thisMethod)) {
                return Reflections.invokeAndUnwrap(self, proceed, args);
            }
            return executeInterception(self, thisMethod, proceed, args, InterceptionType.AROUND_INVOKE, stack);
        }
        return null;
    }

    protected Object executeInterception(Object instance, Method method, Method proceed, Object[] args, InterceptionType interceptionType, Stack stack) throws Throwable {
        CachedInterceptionChain chain = getInterceptionChain(instance, method, interceptionType);
        if (chain.interceptorMethods.isEmpty()) {
            // shortcut if there are no interceptors
            if (proceed == null) {
                return null;
            } else {
                return Reflections.invokeAndUnwrap(instance, proceed, args);
            }
        } else {
            return new WeldInvocationContext(instance, method, proceed, args, chain.interceptorMethods, chain.interceptorBindings, stack.peek()).proceed();
        }
    }

    private CachedInterceptionChain getInterceptionChain(Object instance, Method method, InterceptionType interceptionType) {
        if (method != null) {
            CachedInterceptionChain cachedChain = cachedChains.get(method);
            if (cachedChain == null) {
                cachedChain = new CachedInterceptionChain(ctx.buildInterceptorMethodInvocations(instance, method, interceptionType), ctx.getInterceptionModel()
                        .getMemberInterceptorBindings(method));
                CachedInterceptionChain old = cachedChains.putIfAbsent(method, cachedChain);
                if (old != null) {
                    cachedChain = old;
                }
            }
            return cachedChain;
        }
        return new CachedInterceptionChain(ctx.buildInterceptorMethodInvocations(instance, null, interceptionType), ctx.getInterceptionModel().getClassInterceptorBindings());
    }

    private boolean isInterceptorMethod(Method method) {
        return ctx.getInterceptionModel().getTargetClassInterceptorMetadata().isInterceptorMethod(method);
    }

    private Object readResolve() throws ObjectStreamException {
        return new InterceptorMethodHandler(ctx);
    }

    private static class CachedInterceptionChain {

        private final List<InterceptorMethodInvocation> interceptorMethods;
        private final Set<Annotation> interceptorBindings;

        public CachedInterceptionChain(List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings) {
            this.interceptorMethods = chain;
            this.interceptorBindings = interceptorBindings;
        }
    }
}
