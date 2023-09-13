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
package org.jboss.weld.bean.builtin;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.event.EventImpl;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

public class EventBean extends AbstractFacadeBean<Event<?>> {

    @SuppressWarnings("serial")
    private static final Type DEFAULT_TYPE = new TypeLiteral<Event<Object>>() {
    }.getType();
    private static final Set<Type> TYPES = ImmutableSet.<Type> of(Event.class, Object.class, WeldEvent.class);

    public EventBean(BeanManagerImpl manager) {
        super(manager, Reflections.<Class<Event<?>>> cast(Event.class));
    }

    @Override
    public Class<?> getBeanClass() {
        return EventImpl.class;
    }

    @Override
    protected Event<?> newInstance(InjectionPoint ip, CreationalContext<Event<?>> creationalContext) {
        return EventImpl.of(ip, getBeanManager());
    }

    @Override
    public String toString() {
        return "Implicit Bean [jakarta.enterprise.event.Event] with qualifiers [@Default]";
    }

    @Override
    protected Type getDefaultType() {
        return DEFAULT_TYPE;
    }

    @Override
    public Set<Type> getTypes() {
        return TYPES;
    }
}
