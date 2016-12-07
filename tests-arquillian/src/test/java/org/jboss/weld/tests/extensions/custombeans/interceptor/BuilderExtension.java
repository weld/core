/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.custombeans.interceptor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.bootstrap.event.WeldAfterBeanDiscovery;

public class BuilderExtension implements Extension {

    private AtomicReference<Bean<?>> injectedBean = new AtomicReference<>();
    private AtomicBoolean interceptedWithMetadata = new AtomicBoolean(false);
    private AtomicBoolean intercepted = new AtomicBoolean(false);
    private AtomicInteger counter = new AtomicInteger(0);

    @SuppressWarnings("serial")
    public void afterBeanDiscovery(@Observes WeldAfterBeanDiscovery event) {

        // type level interceptor
        event.addInterceptor().interceptWithMetadata(InterceptionType.AROUND_INVOKE, (invocationContext, fooBean) -> {
            try {
                injectedBean.set(fooBean);
                interceptedWithMetadata.set(true);
                return invocationContext.proceed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).priority(2500).addBinding(TypeBinding.TypeBindingLiteral.INSTANCE);

        // method level interceptor
        event.addInterceptor().intercept(InterceptionType.AROUND_INVOKE, (invocationContext) -> {
            try {
                intercepted.set(true);
                return invocationContext.proceed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).priority(2600).addBinding(MethodBinding.MethodBindingLiteral.INSTANCE);

        // passivation test
        event.addInterceptor().interceptWithMetadata(InterceptionType.AROUND_INVOKE, (invocationContext, barBean) -> {
            try {
                return counter.incrementAndGet() + (Integer) invocationContext.proceed();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).addBinding(PassivatingBinding.PassivatingBindingLiteral.INSTANCE);

    }

    public Bean<?> getInjectedBean() {
        return injectedBean.get();
    }

    public AtomicBoolean getInterceptedWithMetadata() {
        return interceptedWithMetadata;
    }

    public AtomicBoolean getIntercepted() {
        return intercepted;
    }
}
