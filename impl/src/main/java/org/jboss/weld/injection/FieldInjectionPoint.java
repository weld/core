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
package org.jboss.weld.injection;

import static org.jboss.weld.injection.Exceptions.rethrowException;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;

import org.jboss.weld.bean.proxy.DecoratorProxy;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingInjectionPointAttributes;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.security.GetAccessibleCopyOfMember;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "The bean cache is loaded lazily.")
public class FieldInjectionPoint<T, X> extends ForwardingInjectionPointAttributes<T, Field>
        implements WeldInjectionPointAttributes<T, Field>, Serializable {

    /**
     * Creates an injection point without firing the {@link ProcessInjectionPoint} event.
     */
    public static <T, X> FieldInjectionPoint<T, X> silent(FieldInjectionPointAttributes<T, X> attributes) {
        return new FieldInjectionPoint<T, X>(attributes);
    }

    private static final long serialVersionUID = 6645272914499045953L;

    private final boolean cacheable;
    private transient Bean<?> cachedBean;
    private final transient Field accessibleField;

    private final FieldInjectionPointAttributes<T, X> attributes;

    protected FieldInjectionPoint(FieldInjectionPointAttributes<T, X> attributes) {
        this.attributes = attributes;
        this.cacheable = isCacheableInjectionPoint(attributes);
        this.accessibleField = AccessController.doPrivileged(new GetAccessibleCopyOfMember<Field>(attributes.getMember()));
    }

    protected static boolean isCacheableInjectionPoint(WeldInjectionPointAttributes<?, ?> attributes) {
        if (attributes.isDelegate()) {
            return false;
        }
        Class<?> rawType = Reflections.getRawType(attributes.getType());
        return !InjectionPoint.class.isAssignableFrom(rawType) && !Instance.class.isAssignableFrom(rawType);
    }

    public void inject(Object declaringInstance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        try {
            Object instanceToInject = declaringInstance;
            if (!(instanceToInject instanceof DecoratorProxy)) {
                // if declaringInstance is a proxy, unwrap it
                if (declaringInstance instanceof TargetInstanceProxy) {
                    instanceToInject = Reflections.<TargetInstanceProxy<T>> cast(declaringInstance).weld_getTargetInstance();
                }
            }
            Object objectToInject;
            if (!cacheable) {
                objectToInject = manager.getInjectableReference(this, creationalContext);
            } else {
                if (cachedBean == null) {
                    cachedBean = manager.resolve(manager.getBeans(this));
                }
                objectToInject = manager.getInjectableReference(this, cachedBean, creationalContext);
            }
            accessibleField.set(instanceToInject, objectToInject);
        } catch (IllegalArgumentException e) {
            rethrowException(e);
        } catch (IllegalAccessException e) {
            rethrowException(e);
        }
    }

    @Override
    protected FieldInjectionPointAttributes<T, X> delegate() {
        return attributes;
    }

    @Override
    public AnnotatedField<X> getAnnotated() {
        return attributes.getAnnotated();
    }

    private Object readResolve() throws ObjectStreamException {
        return new FieldInjectionPoint<T, X>(attributes);
    }
}
