/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.GET_PREFIX;
import static org.jboss.weld.probe.Strings.IS_PREFIX;
import static org.jboss.weld.probe.Strings.SET_PREFIX;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Invocation.Type;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An invocation monitor interceptor.
 *
 * @author Martin Kouba
 */
@Vetoed
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@Monitored
@Interceptor
@Dependent
public class InvocationMonitor implements Serializable {

    private static final long serialVersionUID = -5245789370968148511L;

    private static final ThreadLocal<Invocation.Builder> INVOCATIONS = new ThreadLocal<Invocation.Builder>();

    private static final AtomicInteger INVOCATION_ID_GENERATOR = new AtomicInteger(0);

    private static final InterceptorAction INTERCEPTOR_ACTION = new InterceptorAction();

    /**
     *
     * @return a new entry point or a child
     */
    static Invocation.Builder initBuilder() {
        return initBuilder(true);
    }

    /**
     *
     * @return a new entry point or a child or <code>null</code>
     */
    static Invocation.Builder initBuilder(boolean initChild) {
        Invocation.Builder builder = INVOCATIONS.get();
        if (builder == null) {
            builder = Invocation.Builder.newBuilder(INVOCATION_ID_GENERATOR.incrementAndGet());
            INVOCATIONS.set(builder);
        } else if (initChild) {
            builder = builder.newChild();
            INVOCATIONS.set(builder);
        }
        return builder;
    }

    @Intercepted
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "According to specification, injected bean has to be serializable.")
    private Bean<?> interceptedBean;

    @Inject
    private BeanManagerImpl beanManager;

    private transient volatile Probe probe = null;

    private transient volatile Boolean skipJavaBeanProperties;

    @AroundConstruct
    public void monitorCreation(InvocationContext ctx) {
        init();
        final Invocation.Builder builder = initBuilder();

        if (interceptedBean != null) {
            builder.setInterceptedBean(interceptedBean);
        } else {
            builder.setDeclaringClassName(ctx.getConstructor().getDeclaringClass().getName());
        }
        builder.setType(Type.CONSTRUCTOR);
        builder.setStart(System.currentTimeMillis());
        builder.setMethodName(ctx.getConstructor().toString());
        try {
            INTERCEPTOR_ACTION.perform(builder, probe, ctx);
        } catch (Exception e) {
            ProbeLogger.LOG.aroundConstructMonitoringProblem(interceptedBean, e);
        }
    }

    @AroundInvoke
    public Object monitor(InvocationContext ctx) throws Exception {
        if (skipJavaBeanProperties && isJavaBeanPropertyAccessor(ctx.getMethod())) {
            // Skip JavaBean accessor methods
            return ctx.proceed();
        }
        init();
        final Invocation.Builder builder = initBuilder();

        if (interceptedBean != null) {
            builder.setInterceptedBean(interceptedBean);
        } else {
            builder.setDeclaringClassName(ctx.getMethod().getDeclaringClass().getName());
        }
        builder.guessType(ctx);
        builder.setStart(System.currentTimeMillis());
        builder.setMethodName(ctx.getMethod().getName());

        return INTERCEPTOR_ACTION.perform(builder, probe, ctx);
    }

    private void init() {
        if (probe == null) {
            synchronized (this) {
                if (probe == null) {
                    Probe probeExt = beanManager.getExtension(ProbeExtension.class).getProbe();
                    if (!probeExt.isInitialized()) {
                        throw ProbeLogger.LOG.probeNotInitialized();
                    }
                    probe = probeExt;
                    skipJavaBeanProperties = beanManager.getServices().get(WeldConfiguration.class)
                            .getBooleanProperty(ConfigurationKey.PROBE_INVOCATION_MONITOR_SKIP_JAVABEAN_PROPERTIES);
                }
            }
        }
    }

    private boolean isJavaBeanPropertyAccessor(Method method) {
        if (method.getParameterCount() == 0) {
            // Getter
            return method.getName().startsWith(GET_PREFIX) || method.getName().startsWith(IS_PREFIX);
        } else if (method.getParameterCount() == 1) {
            // Setter
            return method.getName().startsWith(SET_PREFIX);
        }
        return false;
    }

    abstract static class Action<T> {

        Object perform(Invocation.Builder builder, Probe probe, T context) throws Exception {
            try {
                long start = System.nanoTime();
                Object result = proceed(context);
                builder.setDuration(System.nanoTime() - start);
                if (builder.isEntryPoint()) {
                    if (!builder.isIgnored()) {
                        probe.addInvocation(builder.build());
                    }
                } else {
                    INVOCATIONS.set(builder.getParent());
                }
                return result;
            } finally {
                if (builder.isEntryPoint()) {
                    INVOCATIONS.remove();
                }
            }
        }

        protected abstract Object proceed(T context) throws Exception;

    }

    private static class InterceptorAction extends Action<InvocationContext> {

        @Override
        protected Object proceed(InvocationContext ctx) throws Exception {
            return ctx.proceed();
        }

    }

}
