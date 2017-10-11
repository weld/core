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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.event.EventImpl;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

public class EventBean extends AbstractFacadeBean<Event<?>> {

    private static final Type EVENT_TYPE = new TypeLiteral<Event<Object>>(){
        private static final long serialVersionUID = 3109256773218160485L;
    }.getType();
    private static final Type WELD_EVENT_TYPE = new TypeLiteral<WeldEvent<Object>>(){
        private static final long serialVersionUID = 3109256773218160485L;
    }.getType();
    private static final Set<Type> DEFAULT_TYPES = Arrays2.<Type>asSet(WELD_EVENT_TYPE, EVENT_TYPE);

    public EventBean(BeanManagerImpl manager) {
        super(manager, Reflections.<Class<Event<?>>>cast(Event.class));
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
        return "Implicit Bean [javax.enterprise.event.Event] with qualifiers [@Default]";
    }

    @Override
    protected Type getDefaultType() {
        return EVENT_TYPE;
    }

    public Set<Type> getTypes() {
        return DEFAULT_TYPES;
    }
}
