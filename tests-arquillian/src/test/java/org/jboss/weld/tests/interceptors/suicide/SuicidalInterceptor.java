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
package org.jboss.weld.tests.interceptors.suicide;

import javax.annotation.Priority;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Suicidal
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class SuicidalInterceptor {

    @Inject
    @Intercepted
    private Bean<?> bean;
    @Inject
    private BeanManager manager;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        if (!manager.isNormalScope(bean.getScope())) {
            throw new IllegalStateException("Not a normal-scoped bean:" + bean.toString());
        }
        try {
            return ctx.proceed();
        } catch (Exception e) {
            destroyInstance();
            throw e;
        }
    }

    private void destroyInstance() throws Exception {
        Context context = manager.getContext(bean.getScope());
        if (!(context instanceof AlterableContext)) {
            throw new IllegalStateException("Context does not support removal of instances");
        }
        AlterableContext alterableContext = AlterableContext.class.cast(context);
        alterableContext.destroy(bean);
    }
}
