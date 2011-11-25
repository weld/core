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
package org.jboss.weld.bootstrap.events;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.reflect.Type;

import static org.jboss.weld.logging.messages.BootstrapMessage.ANNOTATION_TYPE_NULL;

/**
 * Container lifecycle event for each Java class or interface discovered by
 * the container.
 *
 * @author pmuir
 * @author David Allen
 */
public class ProcessAnnotatedTypeImpl<X> extends AbstractDefinitionContainerEvent implements ProcessAnnotatedType<X> {

    public static <X> ProcessAnnotatedTypeImpl<X> fire(BeanManagerImpl beanManager, WeldClass<X> clazz) {
        ProcessAnnotatedTypeImpl<X> payload = new ProcessAnnotatedTypeImpl<X>(beanManager, clazz) {
        };
        payload.fire();
        return payload;
    }

    private AnnotatedType<X> annotatedType;
    private boolean veto;
    private boolean dirty;

    public ProcessAnnotatedTypeImpl(BeanManagerImpl beanManager, AnnotatedType<X> annotatedType) {
        super(beanManager, ProcessAnnotatedType.class, new Type[]{annotatedType.getBaseType()});
        this.annotatedType = annotatedType;
    }

    public AnnotatedType<X> getAnnotatedType() {
        return annotatedType;
    }

    public void setAnnotatedType(AnnotatedType<X> type) {
        if (type == null) {
            throw new IllegalArgumentException(ANNOTATION_TYPE_NULL, this);
        }
        this.annotatedType = type;
        this.dirty = true;
    }

    public void veto() {
        this.veto = true;
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public String toString() {
        return annotatedType.toString();
    }

}
