/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.event.ExtensionObserverMethodImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.messages.XmlMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.MetadataImpl;
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

    protected ProcessModuleImpl(BeanManagerImpl beanManager, BeanDeployment deployment) {
        super(beanManager, ProcessModule.class, EMPTY_TYPES);
        this.deployment = deployment;
    }

    @Override
    public Set<Class<?>> getAlternatives() {
        return new SetView<Metadata<Class<?>>, Class<?>>() {
            @Override
            protected Set<Metadata<Class<?>>> getDelegate() {
                return deployment.getEnabledBuilder().getAlternatives();
            }

            @Override
            protected ViewProvider<Metadata<Class<?>>, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public List<Class<?>> getInterceptors() {
        return new ListView<Metadata<Class<?>>, Class<?>>() {
            @Override
            protected List<Metadata<Class<?>>> getDelegate() {
                return deployment.getEnabledBuilder().getInterceptors();
            }

            @Override
            protected ViewProvider<Metadata<Class<?>>, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public List<Class<?>> getDecorators() {
        return new ListView<Metadata<Class<?>>, Class<?>>() {

            @Override
            protected List<Metadata<Class<?>>> getDelegate() {
                return deployment.getEnabledBuilder().getDecorators();
            }

            @Override
            protected ViewProvider<Metadata<Class<?>>, Class<?>> getViewProvider() {
                return viewProvider;
            }
        };
    }

    @Override
    public Iterator<AnnotatedType<?>> getAnnotatedTypes() {
        return cast(deployment.getBeanDeployer().getEnvironment().getAnnotatedTypes().iterator());
    }

    @Override
    public InputStream getBeansXml() {
        try {
            return deployment.getBeanDeploymentArchive().getBeansXml().getUrl().openStream();
        } catch (IOException e) {
            throw new WeldException(XmlMessage.EXCEPTION_OPENING_INPUT_STREAM, e, deployment.getBeanDeploymentArchive().getBeansXml().getUrl());
        }
    }

    private class ClassMetadaViewProvider implements ViewProvider<Metadata<Class<?>>, Class<?>> {

        @Override
        public Class<?> toView(Metadata<Class<?>> from) {
            return from.getValue();
        }

        @Override
        public Metadata<Class<?>> fromView(Class<?> to) {
            StringBuilder location = new StringBuilder();
            location.append(to.getName());
            location.append(" registered by ");
            if (getObserverMethod() instanceof ExtensionObserverMethodImpl<?, ?>) {
                location.append(((ExtensionObserverMethodImpl<?, ?>) getObserverMethod()).getBeanClass().getName());
            } else {
                location.append("an extension.");
            }
            return new MetadataImpl<Class<?>>(to, location.toString());
        }
    }
}
