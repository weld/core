/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.interceptor.util;

import java.util.concurrent.Callable;

import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.interceptor.proxy.InterceptorException;
import org.jboss.weld.interceptor.proxy.LifecycleMixin;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptionUtils {

    public static final String POST_CONSTRUCT = "lifecycle_mixin_$$_postConstruct";

    public static final String PRE_DESTROY = "lifecycle_mixin_$$_preDestroy";

    private static final String WELD_SE_CLASS = "org.jboss.weld.environment.se.Weld";

    private static final String WELD_SERVLET_CLASS = "org.jboss.weld.environment.servlet.WeldServletLifecycle";

    private InterceptionUtils() {
    }

    private static void executePostConstruct(Object proxy, Callable callback) {
        if (proxy instanceof LifecycleMixin) {
            LifecycleMixin lifecycleMixin = (LifecycleMixin) proxy;
            lifecycleMixin.lifecycle_mixin_$$_postConstruct();
        }
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                throw new InterceptorException(e);
            }
        }
    }

    public static void executePostConstruct(Object proxy) {
        executePostConstruct(proxy, null);
    }

    private static void executePredestroy(Object proxy, Callable callback) {
        if (proxy instanceof LifecycleMixin) {
            LifecycleMixin lifecycleMixin = (LifecycleMixin) proxy;
            lifecycleMixin.lifecycle_mixin_$$_preDestroy();
        }
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                throw new InterceptorException(e);
            }
        }
    }

    public static void executePredestroy(Object proxy) {
        executePredestroy(proxy, null);
    }

    static boolean isAnnotationClassExpected(InterceptionType interceptionType) {
        if (InterceptionType.POST_ACTIVATE.equals(interceptionType) || InterceptionType.PRE_PASSIVATE.equals(interceptionType)) {
            Environment environment = detectEnvironment();
            if (environment != null && (Environments.SE.equals(environment) || Environments.SERVLET.equals(environment))) {
                return false;
            }
        }
        return true;
    }

    private static Environment detectEnvironment() {
        // We should rather use the environment from WeldStartup but it's not available in the static initializer
        Environment environment = null;
        if (Reflections.isClassLoadable(WELD_SE_CLASS, WeldClassLoaderResourceLoader.INSTANCE)) {
            environment = Environments.SE;
        } else if (Reflections.isClassLoadable(WELD_SERVLET_CLASS, WeldClassLoaderResourceLoader.INSTANCE)) {
            environment = Environments.SERVLET;
        }
        return environment;
    }
}
