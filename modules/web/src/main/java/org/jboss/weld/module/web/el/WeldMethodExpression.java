/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web.el;

import static org.jboss.weld.module.web.el.ELCreationalContextStack.getCreationalContextStore;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;

import org.jboss.weld.module.web.util.el.ForwardingMethodExpression;

/**
 * @author pmuir
 */
public class WeldMethodExpression extends ForwardingMethodExpression {

    private static final long serialVersionUID = 7070020110515571744L;

    private final MethodExpression delegate;

    public WeldMethodExpression(MethodExpression delegate) {
        this.delegate = delegate;
    }

    @Override
    protected MethodExpression delegate() {
        return delegate;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            return super.invoke(context, params);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            return super.getMethodInfo(context);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

}
