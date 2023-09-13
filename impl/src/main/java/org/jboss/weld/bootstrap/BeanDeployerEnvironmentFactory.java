/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment.WeldMethodKey;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.SetMultimap;

public class BeanDeployerEnvironmentFactory {

    private BeanDeployerEnvironmentFactory() {
    }

    public static BeanDeployerEnvironment newEnvironment(BeanManagerImpl manager) {
        return new BeanDeployerEnvironment(manager);
    }

    /**
     * Creates a new threadsafe BeanDeployerEnvironment instance. These instances are used by {@link ConcurrentBeanDeployer}
     * during bootstrap.
     */
    public static BeanDeployerEnvironment newConcurrentEnvironment(BeanManagerImpl manager) {
        return new BeanDeployerEnvironment(
                Collections.newSetFromMap(new ConcurrentHashMap<SlimAnnotatedTypeContext<?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>()),
                SetMultimap.<Class<?>, AbstractClassBean<?>> newConcurrentSetMultimap(),
                Collections.newSetFromMap(new ConcurrentHashMap<ProducerField<?, ?>, Boolean>()),
                SetMultimap.<WeldMethodKey, ProducerMethod<?, ?>> newConcurrentSetMultimap(),
                Collections.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<ObserverInitializationContext<?, ?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<DisposalMethod<?, ?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<DisposalMethod<?, ?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<DecoratorImpl<?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<InterceptorImpl<?>, Boolean>()),
                Collections.newSetFromMap(new ConcurrentHashMap<Type, Boolean>()), manager);
    }

}
