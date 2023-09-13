/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;

import org.jboss.weld.annotated.EmptyAnnotated;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Tomas Remes
 */
public class ProcessSynthethicBeanImpl<X> extends ProcessBeanImpl<X> implements ProcessSyntheticBean<X> {

    protected static <X> void fire(BeanManagerImpl beanManager, Bean<X> bean, Extension extension) {
        fire(beanManager, bean, EmptyAnnotated.INSTANCE, extension);
    }

    private static <X> void fire(BeanManagerImpl beanManager, Bean<X> bean, Annotated annotated, Extension extension) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessSynthethicBeanImpl<X>(beanManager, bean, annotated, extension) {
            }.fire();
        }
    }

    private final Extension source;

    public ProcessSynthethicBeanImpl(BeanManagerImpl beanManager, Bean<X> bean,
            Annotated annotated, Extension extension) {
        super(beanManager, bean, annotated);
        this.source = extension;
    }

    @Override
    public Extension getSource() {
        return source;
    }

    @Override
    protected Type getRawType() {
        return ProcessSyntheticBean.class;
    }
}
