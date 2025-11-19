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
package org.jboss.weld.resolution;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.inject.Provider;

import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.Primitives;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 * @author alesj
 */
public abstract class AbstractTypeSafeBeanResolver<T extends Bean<?>, C extends Collection<T>>
        extends TypeSafeResolver<Resolvable, T, C, C> {

    private final BeanManagerImpl beanManager;
    private final ComputingCache<Set<Bean<?>>, Set<Bean<?>>> disambiguatedBeans;
    private final MetaAnnotationStore store;

    private final LazyValueHolder<Map<Type, ArrayList<T>>> beansByType;

    public class BeanDisambiguation implements Function<Set<Bean<?>>, Set<Bean<?>>> {

        private BeanDisambiguation() {
        }

        @Override
        public Set<Bean<?>> apply(Set<Bean<?>> from) {
            if (from.size() > 1) {
                ImmutableSet.Builder<Bean<?>> allBeansBuilder = ImmutableSet.builder();
                // beans that are themselves alternatives or their defining bean is an alternative
                Set<Bean<?>> priorityBeans = new HashSet<Bean<?>>();
                // beans that are enabled reserves
                Set<Bean<?>> reserveBeans = new HashSet<Bean<?>>();
                // beans that are neither reserves nor alternatives
                Set<Bean<?>> standardBeans = new HashSet<Bean<?>>();

                for (Bean<?> bean : from) {
                    if (isBeanAlternative(bean)) {
                        // alternatives requires special check to account for declaring bean of producer being
                        // an alternative
                        priorityBeans.add(bean);
                    } else if (bean.isReserve()) {
                        reserveBeans.add(bean);
                    } else {
                        standardBeans.add(bean);
                    }
                    allBeansBuilder.add(bean);
                }
                if (priorityBeans.isEmpty()) {
                    Set<Bean<?>> allBeans = allBeansBuilder.build();
                    if (reserveBeans.size() == allBeans.size()) {
                        // all beans are reserves, we need to disambiguate them
                        return resolveReserves(reserveBeans);
                    } else {
                        // remaining beans are standard beans plus reserves
                        // if there is exactly one non-reserve bean, return it, otherwise return all
                        if (standardBeans.size() == 1) {
                            return Collections.<Bean<?>> singleton(standardBeans.iterator().next());
                        } else {
                            return allBeans;
                        }
                    }
                } else {
                    if (priorityBeans.size() == 1) {
                        return Collections.<Bean<?>> singleton(priorityBeans.iterator().next());
                    } else {
                        return resolveAlternatives(priorityBeans);
                    }
                }
            } else {
                return ImmutableSet.copyOf(from);
            }
        }

        private boolean isBeanAlternative(Bean<?> bean) {
            if (bean.isAlternative()) {
                return true;
            }
            if (bean instanceof AbstractProducerBean<?, ?, ?> producer) {
                return producer.getDeclaringBean().isAlternative();
            }
            return false;
        }

        /**
         * If all the beans left are alternatives with a priority, then the container will select the
         * alternative with the highest priority, and the ambiguous dependency is called resolvable.
         *
         */
        public Set<Bean<?>> resolveAlternatives(Set<Bean<?>> alternatives) {
            int highestPriority = Integer.MIN_VALUE;
            Set<Bean<?>> selectedAlternativesWithHighestPriority = new HashSet<Bean<?>>();
            for (Bean<?> bean : alternatives) {
                Integer priority;
                if (bean instanceof AbstractProducerBean) {
                    // first check for explicit priority declaration on producers
                    priority = ((AbstractProducerBean<?, ?, ?>) bean).getPriority();
                    // if not found, fall back to priority on declaring bean
                    if (priority == null) {
                        priority = beanManager.getEnabled().getAlternativePriority(bean.getBeanClass());
                    }
                } else {
                    priority = beanManager.getEnabled().getAlternativePriority(bean.getBeanClass());
                }
                if (priority == null) {
                    // not all the beans left are alternatives with a priority - we are not able to resolve
                    return ImmutableSet.copyOf(alternatives);
                }
                if (priority > highestPriority) {
                    highestPriority = priority;
                    selectedAlternativesWithHighestPriority.clear();
                }
                if (priority == highestPriority) {
                    selectedAlternativesWithHighestPriority.add(bean);
                }
            }
            return ImmutableSet.copyOf(selectedAlternativesWithHighestPriority);
        }

    }

    /**
     * Otherwise, if there is no bean remaining and all candidate beans are reserves, then:
     * - The container determines the highest priority value and eliminates all candidate beans except for reserves with the
     * highest priority value.
     * - If there is exactly one bean remaining, the container selects this bean, and the ambiguous dependency is called
     * resolvable.
     * - Otherwise, the ambiguous dependency is unresolvable.
     */
    public Set<Bean<?>> resolveReserves(Set<Bean<?>> reserves) {
        int highestPriority = Integer.MIN_VALUE;
        Set<Bean<?>> selectedReservesWithHighestPriority = new HashSet<Bean<?>>();
        for (Bean<?> bean : reserves) {
            Integer priority;
            if (bean instanceof AbstractProducerBean) {
                // first check for explicit priority declaration on producers
                priority = ((AbstractProducerBean<?, ?, ?>) bean).getPriority();
            } else {
                priority = beanManager.getEnabled().getReservePriority(bean.getBeanClass());
            }
            if (priority > highestPriority) {
                highestPriority = priority;
                selectedReservesWithHighestPriority.clear();
            }
            if (priority == highestPriority) {
                selectedReservesWithHighestPriority.add(bean);
            }
        }
        return ImmutableSet.copyOf(selectedReservesWithHighestPriority);

    }

    public AbstractTypeSafeBeanResolver(BeanManagerImpl beanManager, final Iterable<T> beans) {
        super(beans, beanManager.getServices().get(WeldConfiguration.class));
        this.beanManager = beanManager;
        this.disambiguatedBeans = ComputingCacheBuilder.newBuilder().build(new BeanDisambiguation());
        this.store = beanManager.getServices().get(MetaAnnotationStore.class);
        // beansByType stores a map of a type to all beans that are assignable to
        // that type. This means that it most cases we do not need to loop through
        // every bean in the system when performing resolution

        // we build this map lazily, as we do not have access to all beans when
        // the resolver is created. Calling the resolvers clear method will also
        // clear this map.This task is not suitable for a computing hashmap, as
        // the whole map should be calculated in one hit, so only a single
        // iteration over all beans is required

        this.beansByType = new LazyValueHolder<Map<Type, ArrayList<T>>>() {

            @Override
            protected Map<Type, ArrayList<T>> computeValue() {
                Map<Type, ArrayList<T>> map = new HashMap<Type, ArrayList<T>>();
                for (T bean : beans) {
                    mapBean(map, bean);
                }
                trimArrayListsToSize(map);
                return WeldCollections.immutableMapView(map);
            }

            private void mapBean(Map<Type, ArrayList<T>> map, T bean) {
                for (Type type : bean.getTypes()) {
                    mapTypeToBean(map, type, bean);

                    if (type instanceof ParameterizedType) {
                        // we need to add the raw type as well
                        Type rawType = ((ParameterizedType) type).getRawType();
                        mapTypeToBean(map, rawType, bean);
                    } else if (type instanceof Class<?>) {
                        // if the type is a primitive we also need to add the bean
                        // is also resolvable from the boxed class
                        Class<?> clazz = (Class<?>) type;
                        if (clazz.isPrimitive()) {
                            Class<?> wrapped = Primitives.wrap(clazz);
                            mapTypeToBean(map, wrapped, bean);
                        }
                    }
                }
            }

            private void mapTypeToBean(Map<Type, ArrayList<T>> map, Type type, T bean) {
                if (!map.containsKey(type)) {
                    map.put(type, new ArrayList<T>());
                }
                map.get(type).add(bean);
            }

            private void trimArrayListsToSize(Map<Type, ArrayList<T>> map) {
                for (Entry<Type, ArrayList<T>> entry : map.entrySet()) {
                    entry.getValue().trimToSize();
                }
            }
        };
    }

    @Override
    protected boolean matches(Resolvable resolvable, T bean) {
        AssignabilityRules rules = null;
        if (resolvable.isDelegate()) {
            rules = DelegateInjectionPointAssignabilityRules.instance();
        } else {
            rules = BeanTypeAssignabilityRules.instance();
        }
        return rules.matches(resolvable.getTypes(), bean.getTypes())
                && Beans.containsAllQualifiers(resolvable.getQualifiers(), QualifierInstance.of(bean, store));
    }

    @Override
    protected Iterable<? extends T> getAllBeans(Resolvable resolvable) {
        if (resolvable.getTypes().contains(Object.class)
                || Instance.class.equals(resolvable.getJavaClass())
                || Event.class.equals(resolvable.getJavaClass())
                || Provider.class.equals(resolvable.getJavaClass())
                || InterceptionFactory.class.equals(resolvable.getJavaClass())
                || WeldInstance.class.equals(resolvable.getJavaClass())
                || WeldEvent.class.equals(resolvable.getJavaClass())
                || resolvable.getTypes().contains(Serializable.class)) {
            return super.getAllBeans(resolvable);
        }
        Set<T> beans = new HashSet<T>();
        for (Type type : resolvable.getTypes()) {
            beans.addAll(getBeans(type));
            if (type instanceof ParameterizedType) {
                // we also need to consider the raw type
                Type rawType = ((ParameterizedType) type).getRawType();
                beans.addAll(getBeans(rawType));
            } else if (type instanceof Class<?>) {
                // primitives
                Class<?> clazz = (Class<?>) type;
                if (clazz.isPrimitive()) {
                    clazz = Primitives.wrap(clazz);
                    beans.addAll(getBeans(clazz));
                }
            } else if (type instanceof GenericArrayType) {
                Class<Object> rawArrayType = Reflections.getRawType(type);
                beans.addAll(getBeans(rawArrayType));
            }
        }
        return beans;
    }

    private List<T> getBeans(Type type) {
        List<T> beansForType = beansByType.get().get(type);
        return beansForType == null ? Collections.<T> emptyList() : beansForType;
    }

    /**
     * @return the manager
     */
    protected BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    @Override
    protected Set<T> filterResult(Set<T> matched) {
        return Beans.removeDisabledBeans(matched, beanManager);
    }

    public <X> Set<Bean<? extends X>> resolve(Set<Bean<? extends X>> beans) {
        if (beans.isEmpty()) {
            return beans;
        }
        /*
         * We need to defensively copy the beans set as it can be provided by
         * the user in which case this algorithm will have thread safety issues
         */
        //noinspection unchecked
        beans = ImmutableSet.copyOf(beans);
        //noinspection SuspiciousMethodCalls
        return disambiguatedBeans.getCastValue(beans);
    }

    @Override
    public void clear() {
        super.clear();
        this.disambiguatedBeans.clear();
        this.beansByType.clear();
    }

    MetaAnnotationStore getStore() {
        return store;
    }
}
