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
package org.jboss.weld.injection.attributes;

import java.lang.reflect.Field;

import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that infers the attributes by reading
 * {@link EnhancedAnnotatedField}.
 *
 * @author Jozef Hartinger
 *
 */
public class InferringFieldInjectionPointAttributes<T, X> extends AbstractInferringInjectionPointAttributes<T, Field>
        implements FieldInjectionPointAttributes<T, X> {

    private static final long serialVersionUID = -3099189770772787108L;

    public static <T, X> InferringFieldInjectionPointAttributes<T, X> of(EnhancedAnnotatedField<T, X> field, Bean<?> bean,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        return new InferringFieldInjectionPointAttributes<T, X>(field, bean, declaringComponentClass, manager);
    }

    private final AnnotatedField<X> field;

    protected InferringFieldInjectionPointAttributes(EnhancedAnnotatedField<T, X> field, Bean<?> bean,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        super(field, manager.getContextId(), bean, SharedObjectCache.instance(manager).getSharedSet(field.getQualifiers()),
                declaringComponentClass);
        this.field = field.slim();
    }

    @Override
    public Field getMember() {
        return getAnnotated().getJavaMember();
    }

    @Override
    public AnnotatedField<X> getAnnotated() {
        return field;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InferringFieldInjectionPointAttributes<?, ?>) {
            AnnotatedField<?> field = Reflections.<InferringFieldInjectionPointAttributes<?, ?>> cast(obj).getAnnotated();
            return AnnotatedTypes.compareAnnotatedField(getAnnotated(), field);
        }
        return false;
    }
}
