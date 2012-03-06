package org.jboss.weld.interceptor.proxy;

import java.util.Collection;

/**
 * @author Marius Bogoevici
 */
public interface InterceptorInvocation {

    Collection<InterceptorMethodInvocation> getInterceptorMethodInvocations();
}
