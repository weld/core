package org.jboss.weld.interceptor.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private static final long serialVersionUID = 1L;

    private final InterceptionContext ctx;
    private final transient ConcurrentMap<Method, CachedInterceptionChain> cachedChains;

    public InterceptorMethodHandler(InterceptionContext ctx) {
        this.ctx = ctx;
        this.cachedChains = new ConcurrentHashMap<Method, CachedInterceptionChain>();
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
        List<InterceptorMethodInvocation> chain = null;
        Set<Annotation> interceptorBindings = null;
        if (method != null) {
            CachedInterceptionChain cachedChain = cachedChains.get(method);
            if (cachedChain == null) {
                cachedChain = new CachedInterceptionChain(ctx.buildInterceptorMethodInvocations(instance, method, interceptionType), ctx.getInterceptionModel().getMemberInterceptorBindings(method));
                CachedInterceptionChain old = cachedChains.putIfAbsent(method, cachedChain);
                if (old != null) {
                    cachedChain = old;
                }
            }
            chain = cachedChain.chain;
            interceptorBindings = cachedChain.interceptorBindings;
        } else {
            chain = ctx.buildInterceptorMethodInvocations(instance, null, interceptionType);
            interceptorBindings = ctx.getInterceptionModel().getClassInterceptorBindings();
        }
        return new WeldInvocationContext(instance, method, args, chain, interceptorBindings).proceed();
    }

    private boolean isInterceptorMethod(Method method) {
        return ctx.getInterceptionModel().getTargetClassInterceptorMetadata().isInterceptorMethod(method);
    }

    private Object readResolve() throws ObjectStreamException {
        return new InterceptorMethodHandler(ctx);
    }

    private static class CachedInterceptionChain {

        private final List<InterceptorMethodInvocation> chain;
        private final Set<Annotation> interceptorBindings;

        public CachedInterceptionChain(List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings) {
            this.chain = chain;
            this.interceptorBindings = interceptorBindings;
        }
    }
}
