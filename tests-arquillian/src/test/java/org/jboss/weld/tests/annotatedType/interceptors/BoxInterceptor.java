package org.jboss.weld.tests.annotatedType.interceptors;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@BoxBinding
@SuppressWarnings("serial")
public class BoxInterceptor implements Serializable {

    @AroundInvoke
    Object intercept(InvocationContext ctx) throws Exception {
        return true;
    }
}
