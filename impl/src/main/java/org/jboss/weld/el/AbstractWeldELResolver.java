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
package org.jboss.weld.el;

import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.cal10n.LocLogger;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import java.beans.FeatureDescriptor;
import java.lang.annotation.Annotation;
import java.util.Iterator;

import static org.jboss.weld.logging.Category.EL;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ElMessage.PROPERTY_LOOKUP;
import static org.jboss.weld.logging.messages.ElMessage.PROPERTY_RESOLVED;

/**
 * An EL-resolver against the named beans
 *
 * @author Pete Muir
 */
public abstract class AbstractWeldELResolver extends ELResolver {
    private static final LocLogger log = loggerFactory().getLogger(EL);

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
            log.trace(PROPERTY_LOOKUP, propertyString);
            Namespace namespace = null;
            if (base == null) {
                if (beanManager.getRootNamespace().contains(propertyString)) {
                    Object value = beanManager.getRootNamespace().get(propertyString);
                    context.setPropertyResolved(true);
                    log.trace(PROPERTY_RESOLVED, propertyString, value);
                    return value;
                }
            } else if (base instanceof Namespace) {
                namespace = (Namespace) base;
                // We're definitely the responsible party
                context.setPropertyResolved(true);
                if (namespace.contains(propertyString)) {
                    // There is a child namespace
                    Object value = namespace.get(propertyString);
                    log.trace(PROPERTY_RESOLVED, propertyString, value);
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
                log.trace(PROPERTY_RESOLVED, propertyString, value);
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
            ClientProxyProvider cpp = beanManager.getClientProxyProvider();
            if (cpp != null) {
                Object value = cpp.getClientProxy(bean);
                if (value != null) {
                    return value;
                }
            }
            return beanManager.getReference(bean, beanManager.createCreationalContext(bean), false);
        } else {
            // Need to use a "special" creationalContext that can make sure that we do share dependent instances referenced by the EL Expression
            final ELCreationalContext<?> creationalContext = getELCreationalContext(context);
            String beanName = bean.getName();
            Object value = creationalContext.getDependentInstanceForExpression(beanName);
            if (value == null) {
                value = getManager(context).getReference(bean, creationalContext, false);
                creationalContext.registerDependentInstanceForExpression(beanName, value);
            }
            return value;
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
    }

    private ELCreationalContext<?> getELCreationalContext(ELContext context) {
        ELCreationalContextStack stack = ELCreationalContextStack.getCreationalContextStore(context);
        if (!stack.isEmpty()) {
            return stack.peek().get();
        } else {
            throw new IllegalStateException("No CreationalContext registered for EL evaluation, it is likely that the the expression factory has not been wrapped by the CDI BeanManager, which must be done to use the ELResolver from CDI");
        }
    }

}

