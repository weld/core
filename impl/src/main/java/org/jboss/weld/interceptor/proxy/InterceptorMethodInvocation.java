package org.jboss.weld.interceptor.proxy;

import jakarta.interceptor.InvocationContext;

/**
 * @author Marius Bogoevici
 */
public interface InterceptorMethodInvocation {

    Object invoke(InvocationContext invocationContext) throws Exception;

    boolean expectsInvocationContext();
}
