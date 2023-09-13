/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.enablement;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.logging.LogMessageCallback;
import org.jboss.weld.logging.MessageCallback;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This service gathers globally enabled interceptors, decorators and alternatives and builds a list of each.
 *
 * @author Jozef Hartinger
 *
 */
public class GlobalEnablementBuilder extends AbstractBootstrapService {

    private final List<Item> alternatives = Collections.synchronizedList(new ArrayList<Item>());
    private final List<Item> interceptors = Collections.synchronizedList(new ArrayList<Item>());
    private final List<Item> decorators = Collections.synchronizedList(new ArrayList<Item>());

    private volatile Map<Class<?>, Integer> cachedAlternativeMap;
    private volatile boolean sorted;
    private volatile boolean dirty;

    private void addItem(List<Item> list, Class<?> javaClass, int priority) {
        sorted = false;
        dirty = true;
        synchronized (list) {
            int originalPriority = priority;
            if (!list.isEmpty()) {
                int scaling = list.get(0).getNumberOfScaling();
                if (scaling > 0) {
                    // We have to scale the priority if necessary
                    priority *= new BigInteger("" + Item.ITEM_PRIORITY_SCALE_POWER).pow(scaling).intValue();
                }
            }
            list.add(new Item(javaClass, originalPriority, priority));
        }
    }

    public void addAlternative(Class<?> javaClass, int priority) {
        addItem(alternatives, javaClass, priority);
    }

    public void addInterceptor(Class<?> javaClass, int priority) {
        addItem(interceptors, javaClass, priority);
    }

    public void addDecorator(Class<?> javaClass, int priority) {
        addItem(decorators, javaClass, priority);
    }

    public List<Class<?>> getAlternativeList(final Extension extension) {
        initialize();
        return new EnablementListView() {

            @Override
            protected Extension getExtension() {
                return extension;
            }

            @Override
            protected ViewType getViewType() {
                return ViewType.ALTERNATIVES;
            }

            @Override
            protected List<Item> getDelegate() {
                return alternatives;
            }
        };
    }

    public List<Class<?>> getInterceptorList(final Extension extension) {
        initialize();
        return new EnablementListView() {

            @Override
            protected Extension getExtension() {
                return extension;
            }

            @Override
            protected ViewType getViewType() {
                return ViewType.INTERCEPTORS;
            }

            @Override
            protected List<Item> getDelegate() {
                return interceptors;
            }

        };
    }

    public List<Class<?>> getDecoratorList(final Extension extension) {
        initialize();
        return new EnablementListView() {

            @Override
            protected Extension getExtension() {
                return extension;
            }

            @Override
            protected ViewType getViewType() {
                return ViewType.DECORATORS;
            }

            @Override
            protected List<Item> getDelegate() {
                return decorators;
            }
        };
    }

    /**
     *
     * @return <code>true</code> if a new item was added and the up-to-date enablements were not built yet, <code>false</code>
     *         otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /*
     * cachedAlternativeMap is accessed from a single thread only and the result is safely propagated. Therefore, there is no
     * need to synchronize access to
     * cachedAlternativeMap.
     */
    private Map<Class<?>, Integer> getGlobalAlternativeMap() {
        if (cachedAlternativeMap == null || dirty) {
            Map<Class<?>, Integer> map = new HashMap<Class<?>, Integer>();
            for (Item item : alternatives) {
                map.put(item.getJavaClass(), item.getPriority());
            }
            cachedAlternativeMap = ImmutableMap.copyOf(map);
        }
        return cachedAlternativeMap;
    }

    private void initialize() {
        if (!sorted) {
            Collections.sort(alternatives);
            Collections.sort(interceptors);
            Collections.sort(decorators);
            sorted = true;
        }
    }

    public ModuleEnablement createModuleEnablement(BeanDeployment deployment) {

        ClassLoader loader = new ClassLoader(deployment.getBeanManager().getServices().get(ResourceLoader.class));

        BeansXml beansXml = deployment.getBeanDeploymentArchive().getBeansXml();

        Set<Class<?>> alternativeClasses = null;
        Set<Class<? extends Annotation>> alternativeStereotypes = null;

        List<Class<?>> globallyEnabledInterceptors = getInterceptorList(null);
        List<Class<?>> globallyEnabledDecorators = getDecoratorList(null);

        ImmutableList.Builder<Class<?>> moduleInterceptorsBuilder = ImmutableList.<Class<?>> builder();
        moduleInterceptorsBuilder.addAll(globallyEnabledInterceptors);

        ImmutableList.Builder<Class<?>> moduleDecoratorsBuilder = ImmutableList.<Class<?>> builder();
        moduleDecoratorsBuilder.addAll(globallyEnabledDecorators);

        if (beansXml != null) {

            checkForDuplicates(beansXml.getEnabledInterceptors(), ValidatorLogger.INTERCEPTOR_SPECIFIED_TWICE);
            checkForDuplicates(beansXml.getEnabledDecorators(), ValidatorLogger.DECORATOR_SPECIFIED_TWICE);
            checkForDuplicates(beansXml.getEnabledAlternativeClasses(),
                    ValidatorLogger.ALTERNATIVE_CLASS_SPECIFIED_MULTIPLE_TIMES);
            checkForDuplicates(beansXml.getEnabledAlternativeStereotypes(),
                    ValidatorLogger.ALTERNATIVE_STEREOTYPE_SPECIFIED_MULTIPLE_TIMES);

            List<Class<?>> interceptorClasses = beansXml.getEnabledInterceptors().stream().map(loader)
                    .collect(Collectors.toList());
            moduleInterceptorsBuilder.addAll(filter(interceptorClasses, globallyEnabledInterceptors,
                    ValidatorLogger.INTERCEPTOR_ENABLED_FOR_APP_AND_ARCHIVE,
                    deployment));

            List<Class<?>> decoratorClasses = beansXml.getEnabledDecorators().stream().map(loader).collect(Collectors.toList());
            moduleDecoratorsBuilder.addAll(
                    filter(decoratorClasses, globallyEnabledDecorators, ValidatorLogger.DECORATOR_ENABLED_FOR_APP_AND_ARCHIVE,
                            deployment));

            alternativeClasses = beansXml.getEnabledAlternativeClasses().stream().map(loader).collect(ImmutableSet.collector());
            alternativeStereotypes = cast(
                    beansXml.getEnabledAlternativeStereotypes().stream().map(loader).collect(ImmutableSet.collector()));
        } else {
            alternativeClasses = Collections.emptySet();
            alternativeStereotypes = Collections.emptySet();
        }

        Map<Class<?>, Integer> globalAlternatives = getGlobalAlternativeMap();

        // We suppose that enablements are always created all at once
        dirty = false;

        return new ModuleEnablement(moduleInterceptorsBuilder.build(), moduleDecoratorsBuilder.build(), globalAlternatives,
                alternativeClasses,
                alternativeStereotypes);
    }

    @Override
    public void cleanupAfterBoot() {
        alternatives.clear();
        interceptors.clear();
        decorators.clear();
    }

    @Override
    public String toString() {
        return "GlobalEnablementBuilder [alternatives=" + alternatives + ", interceptors=" + interceptors + ", decorators="
                + decorators + "]";
    }

    private <T> void checkForDuplicates(List<Metadata<T>> list, MessageCallback<DeploymentException> messageCallback) {
        Map<T, Metadata<T>> map = new HashMap<T, Metadata<T>>();
        for (Metadata<T> item : list) {
            Metadata<T> previousOccurrence = map.put(item.getValue(), item);
            if (previousOccurrence != null) {
                throw messageCallback.construct(item.getValue(), item, previousOccurrence);
            }
        }
    }

    /**
     * Filter out interceptors and decorators which are also enabled globally.
     *
     * @param enabledClasses
     * @param globallyEnabledClasses
     * @param logMessageCallback
     * @param deployment
     * @return the filtered list
     */
    private <T> List<Class<?>> filter(List<Class<?>> enabledClasses, List<Class<?>> globallyEnabledClasses,
            LogMessageCallback logMessageCallback,
            BeanDeployment deployment) {
        for (Iterator<Class<?>> iterator = enabledClasses.iterator(); iterator.hasNext();) {
            Class<?> enabledClass = iterator.next();
            if (globallyEnabledClasses.contains(enabledClass)) {
                logMessageCallback.log(enabledClass, deployment.getBeanDeploymentArchive().getId());
                iterator.remove();
            }
        }
        return enabledClasses;
    }

    private static class ClassLoader implements Function<Metadata<String>, Class<?>> {

        private final ResourceLoader resourceLoader;

        public ClassLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public Class<?> apply(Metadata<String> from) {
            try {
                return resourceLoader.classForName(from.getValue());
            } catch (ResourceLoadingException e) {
                throw BootstrapLogger.LOG.errorLoadingBeansXmlEntry(from.getValue(), from.getLocation(), e.getCause());
            } catch (Exception e) {
                throw BootstrapLogger.LOG.errorLoadingBeansXmlEntry(from.getValue(), from.getLocation(), e);
            }
        }
    }
}
