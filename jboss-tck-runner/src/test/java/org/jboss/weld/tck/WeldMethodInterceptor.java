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
package org.jboss.weld.tck;

import static org.jboss.weld.config.ConfigurationKey.RELAXED_CONSTRUCTION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.cdi.tck.impl.testng.SingleTestClassMethodInterceptor;
import org.jboss.cdi.tck.tests.implementation.simple.lifecycle.unproxyable.UnproxyableManagedBeanTest;
import org.jboss.cdi.tck.tests.lookup.clientProxy.unproxyable.beanConstructor.BeanConstructorWithParametersTest;
import org.jboss.cdi.tck.tests.lookup.clientProxy.unproxyable.privateConstructor.PrivateConstructorTest;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.util.collections.ImmutableSet;
import org.testng.IMethodInstance;
import org.testng.ITestContext;

/**
 * If unsafe proxies are enabled, this interceptor disables a set of TCK tests that are known to fail with unsafe proxies
 * (because with unsafe proxies Weld is
 * less strict than required).
 *
 * @author Jozef Hartinger
 *
 */
public class WeldMethodInterceptor extends SingleTestClassMethodInterceptor {

    private static final String ADDITIONAL_VM_ARGS_PROPERTY = "additional.vm.args";
    private static final Set<String> RELAXED_CONSTRUCTION_EXCLUDED_TESTS;
    private static final Logger LOG = Logger.getLogger(WeldMethodInterceptor.class.getName());

    static {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.add(UnproxyableManagedBeanTest.class.getName());
        builder.add(BeanConstructorWithParametersTest.class.getName());
        builder.add(PrivateConstructorTest.class.getName());
        RELAXED_CONSTRUCTION_EXCLUDED_TESTS = builder.build();
    }

    private boolean isUnsafeProxyModeEnabled() {
        String additional = System.getProperty(ADDITIONAL_VM_ARGS_PROPERTY, "");
        if (additional.contains(RELAXED_CONSTRUCTION.get()) && !additional.contains(RELAXED_CONSTRUCTION.get() + '=' + false)) {
            return true;
        }
        return Boolean.valueOf(System.getProperty(ConfigurationKey.RELAXED_CONSTRUCTION.get(), "false"));
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (isUnsafeProxyModeEnabled()) {
            // exclude certain tests
            LOG.log(Level.INFO, "Relaxed construction mode enabled");
            methods = new ArrayList<IMethodInstance>(methods);
            for (Iterator<IMethodInstance> iterator = methods.iterator(); iterator.hasNext();) {
                if (RELAXED_CONSTRUCTION_EXCLUDED_TESTS.contains(iterator.next().getMethod().getRealClass().getName())) {
                    iterator.remove();
                }
            }
        }
        return super.intercept(methods, context);
    }
}
