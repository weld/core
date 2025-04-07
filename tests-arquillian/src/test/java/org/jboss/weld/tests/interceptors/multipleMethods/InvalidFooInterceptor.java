package org.jboss.weld.tests.interceptors.multipleMethods;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Priority(1)
@MyBinding
public class InvalidFooInterceptor {

    // Note that WFLY does similar detection for around invoke already
    // https://github.com/wildfly/wildfly/blob/36.0.0.Beta1/ee/src/main/java/org/jboss/as/ee/logging/EeLogger.java#L1061-L1070

    @PostConstruct
    public Object method1(InvocationContext ctx) throws Exception {
        return ctx.proceed() + "intercept2";
    }

    @PostConstruct
    public Object method2(InvocationContext ctx) throws Exception {
        return ctx.proceed() + "intercept";
    }

}
