/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.Instance;

import org.jboss.cdi.tck.spi.Contexts;
import org.jboss.weld.Container;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.ForwardingContext;

public class ContextsImpl implements Contexts<Context> {

    public RequestContext getRequestContext() {
        BeanManagerImpl beanManager = Container.instance().deploymentManager();
        // Active req. context impl will differ between running -Dincontainer (WFLY) and embedded tests
        Instance<HttpRequestContext> httpReqContext = beanManager.instance().select(HttpRequestContext.class);
        if (httpReqContext.isResolvable()) {
            return httpReqContext.get();
        } else {
            return beanManager.instance().select(BoundRequestContext.class).get();
        }
    }

    public void setActive(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ((ManagedContext) context).activate();
        } else if (context instanceof ApplicationContext) {
            // No-op, always active
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void setInactive(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ((ManagedContext) context).deactivate();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public DependentContext getDependentContext() {
        return Container.instance().deploymentManager().instance().select(DependentContext.class).get();
    }

    public void destroyContext(Context context) {
        context = ForwardingContext.unwrap(context);
        if (context instanceof ManagedContext) {
            ManagedContext managedContext = (ManagedContext) context;
            managedContext.invalidate();
            managedContext.deactivate();
            managedContext.activate();
        } else if (context instanceof ApplicationContext) {
            ((ApplicationContext) context).invalidate();
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
