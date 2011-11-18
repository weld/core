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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.primitives.Primitives;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Provider;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * @author pmuir
 */
public class TypeSafeBeanResolver<T extends Bean<?>> extends TypeSafeResolver<Resolvable, T> {

    private final BeanManagerImpl beanManager;
    private final ConcurrentMap<Set<Bean<?>>, Set<Bean<?>>> disambiguatedBeans;

    private final LazyValueHolder<Map<Type, ArrayList<T>>> beansByType;

    public static class BeanDisambiguation implements Function<Set<Bean<?>>, Set<Bean<?>>> {

        private BeanDisambiguation() {
        }

        public Set<Bean<?>> apply(Set<Bean<?>> from) {
            if (from.size() > 1) {
                boolean alternativePresent = Beans.isAlternativePresent(from);
                Set<Bean<?>> disambiguatedBeans = new HashSet<Bean<?>>();

                for (Bean<?> bean : from) {
                    if (alternativePresent == false || bean.isAlternative()) {
                        disambiguatedBeans.add(bean);
                    }
                }
                return ImmutableSet.copyOf(disambiguatedBeans);
            } else {
                return ImmutableSet.copyOf(from);
            }
        }

    }

    public TypeSafeBeanResolver(BeanManagerImpl beanManager, final Iterable<T> beans) {
        super(beans);
        this.beanManager = beanManager;
        this.disambiguatedBeans = new MapMaker().makeComputingMap(new BeanDisambiguation());
        // beansByType stores a map of a type to all beans that are assignable to
        // that type. This means that it most cases we do not need to loop through
        // every bean in the system when performing resolution

        // we build this map lazily, as we do not have access to all beans when
        // the resolveris created. Calling the resolvers clear method will also
        // clear this map.This task is not suitable for a computing hashmap, as
        // the whole map should be calculated in one hit, so only a single
        // iteration over all beans is required

        this.beansByType = new LazyValueHolder<Map<Type, ArrayList<T>>>() {

            @Override
            protected Map<Type, ArrayList<T>> computeValue() {
                Map<Type, ArrayList<T>> val = new HashMap<Type, ArrayList<T>>();
                for (T bean : beans) {
                    for (Type type : bean.getTypes()) {
                        if (!val.containsKey(type)) {
                            val.put(type, new ArrayList<T>());
                        }
                        val.get(type).add(bean);
                        if (type instanceof ParameterizedType) {
                            // we need to add the raw type as well
                            Type rawType = ((ParameterizedType) type).getRawType();
                            if (!val.containsKey(rawType)) {
                                val.put(rawType, new ArrayList<T>());
                            }
                            val.get(rawType).add(bean);
                        } else if (type instanceof Class<?>) {
                            // if the type is a primitive we also need to add the bean
                            // is also resolvable from the boxed class
                            Class<?> clazz = (Class<?>) type;
                            if (clazz.isPrimitive()) {
                                clazz = Primitives.wrap(clazz);
                                if (!val.containsKey(clazz)) {
                                    val.put(clazz, new ArrayList<T>());
                                }
                                val.get(clazz).add(bean);
                            }
                        }
                    }
                }
                for (Entry<Type, ArrayList<T>> entry : val.entrySet()) {
                    entry.getValue().trimToSize();
                }
                return Collections.unmodifiableMap(val);
            }
        };

    }

    @Override
    protected boolean matches(Resolvable resolvable, T bean) {
        return Reflections.matches(resolvable.getTypes(), bean.getTypes()) && Beans.containsAllQualifiers(resolvable.getQualifiers(), bean.getQualifiers(), beanManager);
    }

    @Override
    protected Iterable<? extends T> getAllBeans(Resolvable resolvable) {
        if (resolvable.getTypes().contains(Object.class) || Instance.class.equals(resolvable.getJavaClass()) || Event.class.equals(resolvable.getJavaClass()) || Provider.class.equals(resolvable.getJavaClass()) || resolvable.getTypes().contains(Serializable.class)) {
            return super.getAllBeans(resolvable);
        }
        Set<T> beans = new HashSet<T>();
        for (Type type : resolvable.getTypes()) {
            List<T> beansForType = beansByType.get().get(type);
            if (beansForType != null) {
                beans.addAll(beansForType);
            }
            if (type instanceof ParameterizedType) {
                // we also need to consider the raw type
                Type rawType = ((ParameterizedType) type).getRawType();
                beansForType = beansByType.get().get(rawType);
                if (beansForType != null) {
                    beans.addAll(beansForType);
                }
            } else if (type instanceof Class<?>) {
                // primitives
                Class<?> clazz = (Class<?>) type;
                if (clazz.isPrimitive()) {
                    clazz = Primitives.wrap(clazz);
                    beansForType = beansByType.get().get(clazz);
                    if (beansForType != null) {
                        beans.addAll(beansForType);
                    }
                }
            }
        }
        return beans;
    }

    /**
     * @return the manager
     */
    protected BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    @Override
    protected Set<T> filterResult(Set<T> matched) {
        return Beans.removeDisabledAndSpecializedBeans(matched, beanManager);
    }

    @Override
    protected Set<T> sortResult(Set<T> matched) {
        return matched;
    }

    public <X> Set<Bean<? extends X>> resolve(Set<Bean<? extends X>> beans) {
        if (beans.size() <= 1) {
            return beans;
        }
        /*
        * We need to defensively copy the beans set as it can be provided by
        * the user in which case this algorithm will have thread safety issues
        */
        beans = ImmutableSet.copyOf(beans);
        return cast(disambiguatedBeans.get(beans));
    }

    @Override
    public void clear() {
        super.clear();
        this.disambiguatedBeans.clear();
        this.beansByType.clear();
    }

}
