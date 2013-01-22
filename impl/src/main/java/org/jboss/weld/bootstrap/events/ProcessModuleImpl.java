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
package org.jboss.weld.bootstrap.events;

import static org.jboss.weld.util.reflection.Reflections.EMPTY_TYPES;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessModule;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.enablement.ClassEnablement;
import org.jboss.weld.bootstrap.enablement.ModuleEnablementBuilder;
import org.jboss.weld.event.ExtensionObserverMethodImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.messages.XmlMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ListToSet;
import org.jboss.weld.util.collections.ListView;
import org.jboss.weld.util.collections.SetView;
import org.jboss.weld.util.collections.ViewProvider;

/**
 *
 * @author Jozef Hartinger
 *
 */
public class ProcessModuleImpl extends AbstractDeploymentContainerEvent implements ProcessModule {

    public static ProcessModuleImpl fire(BeanDeployment deployment) {
        ProcessModuleImpl event = new ProcessModuleImpl(deployment.getBeanManager(), deployment);
        event.fire();
        return event;
    }

    private final BeanDeployment deployment;
    private final ClassMetadaViewProvider viewProvider = new ClassMetadaViewProvider();
    private final ModuleEnablementBuilder moduleEnablementBuilder;

    protected ProcessModuleImpl(BeanManagerImpl beanManager, BeanDeployment deployment) {
        super(beanManager, ProcessModule.class, EMPTY_TYPES);
        this.deployment = deployment;
        this.moduleEnablementBuilder = deployment.getModuleEnablementBuilder();
    }

    @Override
    public List<Class<?>> getAlternatives() {
        return new ListView<ClassEnablement, Class<?>>() {

            @Override
            protected List<ClassEnablement> getDelegate() {
                return moduleEnablementBuilder.getAlternatives();
            }

            @Override
            protected ViewProvider<ClassEnablement, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public List<Class<?>> getInterceptors() {
        return new ListView<ClassEnablement, Class<?>>() {
            @Override
            protected List<ClassEnablement> getDelegate() {
                return moduleEnablementBuilder.getInterceptors();
            }

            @Override
            protected ViewProvider<ClassEnablement, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public List<Class<?>> getDecorators() {
        return new ListView<ClassEnablement, Class<?>>() {

            @Override
            protected List<ClassEnablement> getDelegate() {
                return moduleEnablementBuilder.getDecorators();
            }

            @Override
            protected ViewProvider<ClassEnablement, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public InputStream getBeansXml() {
        try {
            return deployment.getBeanDeploymentArchive().getBeansXml().getUrl().openStream();
        } catch (IOException e) {
            throw new WeldException(XmlMessage.EXCEPTION_OPENING_INPUT_STREAM, e, deployment.getBeanDeploymentArchive().getBeansXml().getUrl());
        }
    }

    private class ClassMetadaViewProvider implements ViewProvider<ClassEnablement, Class<?>> {

        @Override
        public Class<?> toView(ClassEnablement from) {
            return from.getEnabledClass();
        }

        @Override
        public ClassEnablement fromView(Class<?> to) {
            StringBuilder location = new StringBuilder();
            location.append(to.getName());
            location.append(" registered by ");
            if (getObserverMethod() instanceof ExtensionObserverMethodImpl<?, ?>) {
                location.append(((ExtensionObserverMethodImpl<?, ?>) getObserverMethod()).getBeanClass().getName());
            } else {
                location.append("an extension.");
            }
            return new ClassEnablement(to, location.toString(), null);
        }
    }
}
