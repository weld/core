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
import jakarta.el.ValueExpression;

import org.jboss.weld.module.web.util.el.ForwardingValueExpression;

/**
 * @author pmuir
 * @author alesj
 */
public class WeldValueExpression extends ForwardingValueExpression {

    private static final long serialVersionUID = 1122137212009930853L;

    private final ValueExpression delegate;

    public WeldValueExpression(ValueExpression delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ValueExpression delegate() {
        return delegate;
    }

    @Override
    public Object getValue(final ELContext context) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            return delegate().getValue(context);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

    @Override
    public void setValue(ELContext context, Object value) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            delegate().setValue(context, value);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            return delegate().isReadOnly(context);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getType(ELContext context) {
        ELCreationalContextStack store = getCreationalContextStore(context);
        try {
            store.push(new CreationalContextCallable());
            return delegate().getType(context);
        } finally {
            CreationalContextCallable callable = store.pop();
            if (callable.exists()) {
                callable.get().release();
            }
        }
    }

}
