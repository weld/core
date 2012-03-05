package org.jboss.weld.interceptor.proxy;

import javax.interceptor.InvocationContext;

/**
 * @author Marius Bogoevici
 */
public interface InterceptorMethodInvocation {

    Object invoke(InvocationContext invocationContext) throws Exception;

    boolean expectsInvocationContext();
}
