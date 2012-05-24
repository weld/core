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
package org.jboss.weld.enums;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
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

    public <T extends Enum<?>> void addEnumClass(AnnotatedType<T> annotatedType) {
        if (injectionTargets.containsKey(annotatedType.getJavaClass())) {
            return;
        }
        EnhancedAnnotatedType<T> weldClass = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(annotatedType);
        EnumInjectionTarget<? extends Enum<?>> enumInjectionTarget = EnumInjectionTarget.of(weldClass, manager);
        injectionTargets.put(weldClass.getJavaClass(), enumInjectionTarget);
        manager.getServices().get(InjectionTargetService.class).addInjectionTargetToBeValidated(enumInjectionTarget);
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
