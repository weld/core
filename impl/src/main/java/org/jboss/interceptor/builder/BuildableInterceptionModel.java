package org.jboss.interceptor.builder;

import java.lang.reflect.Method;

import org.jboss.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.interceptor.spi.model.InterceptionModel;
import org.jboss.interceptor.spi.model.InterceptionType;

/**
 * An interception model that can be manipulated by the builder.
 *
 * @author Marius Bogoevici
 */
public interface BuildableInterceptionModel<T, I> extends InterceptionModel<T, I> {

    void setIgnoresGlobals(Method m, boolean b);

    void appendInterceptors(InterceptionType interceptionType, Method method, InterceptorMetadata<I>... interceptors);

}
