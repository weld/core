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

import java.beans.FeatureDescriptor;
import java.lang.annotation.Annotation;
import java.util.Iterator;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.logging.ElLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * An EL-resolver against the named beans
 *
 * @author Pete Muir
 */
public abstract class AbstractWeldELResolver extends ELResolver {

    protected abstract BeanManagerImpl getManager(ELContext context);

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return null;
    }

    @Override
    public Object getValue(final ELContext context, Object base, Object property) {
        BeanManagerImpl beanManager = getManager(context);
        if (property != null) {
            String propertyString = property.toString();
            ElLogger.LOG.propertyLookup(propertyString);
            Namespace namespace = null;
            if (base == null) {
                if (getRootNamespace().contains(propertyString)) {
                    Object value = getRootNamespace().get(propertyString);
                    context.setPropertyResolved(true);
                    ElLogger.LOG.propertyResolved(propertyString, value);
                    return value;
                }
            } else if (base instanceof Namespace) {
                namespace = (Namespace) base;
                // We're definitely the responsible party
                context.setPropertyResolved(true);
                if (namespace.contains(propertyString)) {
                    // There is a child namespace
                    Object value = namespace.get(propertyString);
                    ElLogger.LOG.propertyResolved(propertyString, value);
                    return value;
                }
            } else {
                // let the standard EL resolver chain handle the property
                return null;
            }
            final String name;
            if (namespace != null) {
                // Try looking in the manager for a bean
                name = namespace.qualifyName(propertyString);
            } else {
                name = propertyString;
            }
            Object value = lookup(beanManager, context, name);
            if (value != null) {
                context.setPropertyResolved(true);
                ElLogger.LOG.propertyResolved(propertyString, value);
                return value;
            }
        }
        return null;
    }

    private Object lookup(BeanManagerImpl beanManager, ELContext context, String name) {
        final Bean<?> bean = beanManager.resolve(beanManager.getBeans(name));
        if (bean == null) {
            return null;
        }
        Class<? extends Annotation> scope = bean.getScope();
        if (!scope.equals(Dependent.class)) {
            return beanManager.getReference(bean, null, beanManager.createCreationalContext(bean), true);
        } else {
            // Need to use a "special" creationalContext that can make sure that we do share dependent instances referenced by the EL Expression
            final ELCreationalContextStack stack = ELCreationalContextStack.getCreationalContextStore(context);
            boolean release = stack.isEmpty(); // indicates whether we should cleanup after lookup or not
            if (release) {
                stack.push(new CreationalContextCallable());
            }
            try {
                ELCreationalContext<?> ctx = stack.peek().get();
                String beanName = bean.getName();
                Object value = ctx.getDependentInstanceForExpression(beanName);
                if (value == null) {
                    value = getManager(context).getReference(bean, null, ctx, true);
                    ctx.registerDependentInstanceForExpression(beanName, value);
                }
                return value;
            } finally {
                if (release) {
                    CreationalContextCallable callable = stack.pop();
                    if (callable.exists()) {
                        callable.get().release();
                    }
                }
            }

        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
    }

    protected abstract Namespace getRootNamespace();
}
