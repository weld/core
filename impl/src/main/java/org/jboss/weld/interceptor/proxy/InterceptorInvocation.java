package org.jboss.weld.interceptor.proxy;

import java.util.List;

/**
 * Represents an invocation of interceptor's methods on an interceptor instance.
 *
 * @author Marius Bogoevici
 */
public interface InterceptorInvocation {

    /**
     * Returns {@link InterceptorMethodInvocation} objects representing interceptor methods to be invoked during an interceptor
     * invocation.
     * <p>
     * Each of the {@link InterceptorMethodInvocation} should be invoked as part of the invocation chain. The methods should be
     * invoked in the given order.
     *
     * @return interceptor method invocation list
     */
    List<InterceptorMethodInvocation> getInterceptorMethodInvocations();
}
