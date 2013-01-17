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
package org.jboss.weld.enums;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Injects and disinjects Java enums.
 *
 * @author Jozef Hartinger
 *
 */
public class EnumService implements Service {

    // Only needed at bootstrap. Used for matching enum instances with matching WeldClass instances.
    private final Map<Class<? extends Enum<?>>, EnumInjectionTarget<? extends Enum<?>>> injectionTargets;

    private final Set<EnumInstanceContext<?>> instances;
    private final BeanManagerImpl manager;
    private boolean injected;

    public EnumService(BeanManagerImpl manager) {
        this.manager = manager;
        this.injectionTargets = new HashMap<Class<? extends Enum<?>>, EnumInjectionTarget<? extends Enum<?>>>();
        this.instances = new HashSet<EnumInstanceContext<?>>();
    }

    public <T extends Enum<?>> void addEnumClass(SlimAnnotatedType<T> annotatedType) {
        if (injectionTargets.containsKey(annotatedType.getJavaClass())) {
            return;
        }
        EnhancedAnnotatedType<T> weldClass = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(annotatedType);
        EnumInjectionTarget<? extends Enum<?>> enumInjectionTarget = EnumInjectionTarget.of(weldClass, manager);
        injectionTargets.put(weldClass.getJavaClass(), enumInjectionTarget);
        manager.getServices().get(InjectionTargetService.class).validateProducer(enumInjectionTarget);
    }

    public void inject() {
        if (injected) {
            throw new IllegalStateException(BootstrapMessage.ENUMS_ALREADY_INJECTED);
        }
        initInstances();
        for (EnumInstanceContext<?> instance : instances) {
            instance.inject();
        }
        injected = true;
    }

    public void disinject() {
        if (injected) { // may not have been injected if shutting down after deployment error
            for (EnumInstanceContext<?> instance : instances) {
                instance.destroy();
            }
            injected = false;
        }
    }

    public Collection<EnumInjectionTarget<? extends Enum<?>>> getEnumInjectionTargets() {
        return Collections.unmodifiableCollection(injectionTargets.values());
    }

    protected void initInstances() {
        for (Class<? extends Enum<?>> clazz : injectionTargets.keySet()) {
            if (clazz.isEnum()) {
                for (Enum<?> instance : clazz.getEnumConstants()) {
                    addInstance(instance, injectionTargets.get(instance.getClass()));
                }
            }
        }
        injectionTargets.clear(); // no longer needed
    }

    protected <T extends Enum<?>> void addInstance(Enum<?> instance, EnumInjectionTarget<T> injector) {
        if (injector == null) {
            throw new IllegalStateException(BootstrapMessage.ENUM_INJECTION_TARGET_NOT_CREATED, instance);
        }
        instances.add(new EnumInstanceContext<T>(Reflections.<T> cast(instance), injector, manager.<T> createCreationalContext(null)));
    }

    public void cleanup() {
        instances.clear();
    }
}
