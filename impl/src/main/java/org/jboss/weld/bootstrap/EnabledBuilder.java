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
package org.jboss.weld.bootstrap;

import static com.google.common.collect.Lists.transform;
import static org.jboss.weld.logging.messages.BootstrapMessage.ERROR_LOADING_BEANS_XML_ENTRY;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_NOT_CLASS;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_CLASS_SPECIFIED_MULTIPLE_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.ProcessModule;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.EnabledClass;
import org.jboss.weld.bootstrap.spi.EnabledStereotype;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.manager.Enabled;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;

/**
 * Used for building {@link Enabled} during {@link ProcessModule} phase.
 *
 * @author Nicklas Karlsson
 * @author Ales Justin
 * @author Jozef Hartinger
 *
 */
public class EnabledBuilder {

    private static class ClassLoader<T> implements Function<Metadata<EnabledClass>, Metadata<Class<? extends T>>> {

        private final ResourceLoader resourceLoader;

        public ClassLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        public Metadata<Class<? extends T>> apply(Metadata<EnabledClass> from) {
            String location = from.getLocation();
            MetadataImpl<Class<? extends T>> metadata = null;
            try {
                metadata = new MetadataImpl<Class<? extends T>>(Reflections.<Class<? extends T>> cast(resourceLoader
                        .classForName(from.getValue().getValue())), location);
            } catch (ResourceLoadingException e) {
                throw new DeploymentException(ERROR_LOADING_BEANS_XML_ENTRY, e.getCause(), from.getValue(), from.getLocation());
            } catch (Exception e) {
                throw new DeploymentException(ERROR_LOADING_BEANS_XML_ENTRY, e, from.getValue(), from.getLocation());
            }
            if (from.getValue() instanceof EnabledStereotype) {
                if (!metadata.getValue().isAnnotation()) {
                    throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE, metadata.getValue());
                }
            } else {
                if (metadata.getValue().isAnnotation() || metadata.getValue().isInterface()) {
                    throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_CLASS, metadata.getValue());
                }
            }
            return metadata;
        }
    }

    private final List<Metadata<Class<?>>> decorators;
    private final List<Metadata<Class<?>>> interceptors;
    private final Set<Metadata<Class<?>>> alternatives;

    public static EnabledBuilder of(BeansXml beansXml, ResourceLoader resourceLoader) {
        if (beansXml == null) {
            return new EnabledBuilder();
        }
        ClassLoader<Object> classLoader = new ClassLoader<Object>(resourceLoader);
        return new EnabledBuilder(
                new ArrayList<Metadata<Class<?>>>(transform(beansXml.getEnabledDecorators(), classLoader)),
                new ArrayList<Metadata<Class<?>>>(transform(beansXml.getEnabledInterceptors(), classLoader)),
                listToSet(transform(beansXml.getEnabledAlternatives(), classLoader), ALTERNATIVE_CLASS_SPECIFIED_MULTIPLE_TIMES));
    }

    protected EnabledBuilder() {
        this(new ArrayList<Metadata<Class<?>>>(), new ArrayList<Metadata<Class<?>>>(), new HashSet<Metadata<Class<?>>>());
    }

    protected EnabledBuilder(List<Metadata<Class<?>>> decorators, List<Metadata<Class<?>>> interceptors, Set<Metadata<Class<?>>> alternatives) {
        this.decorators = decorators;
        this.interceptors = interceptors;
        this.alternatives = alternatives;
    }

    private static <T> Set<T> listToSet(List<T> list, ValidatorMessage duplicateMessage) {
        Set<T> result = new HashSet<T>();
        for (T item : list) {
            // this check is done once again in Enabled after ProcessModule is fired
            if (!result.add(item)) {
                throw new DeploymentException(duplicateMessage, item);
            }
        }
        return result;
    }

    public List<Metadata<Class<?>>> getDecorators() {
        return decorators;
    }

    public List<Metadata<Class<?>>> getInterceptors() {
        return interceptors;
    }

    public Set<Metadata<Class<?>>> getAlternatives() {
        return alternatives;
    }

    public Enabled create() {
        // create defensive copies since extension may keep references to these structures
        return new Enabled(new HashSet<Metadata<Class<?>>>(alternatives),
                new ArrayList<Metadata<Class<?>>>(decorators),
                new ArrayList<Metadata<Class<?>>>(interceptors));
    }

    public void clear() {
        decorators.clear();
        interceptors.clear();
        alternatives.clear();
    }
}
