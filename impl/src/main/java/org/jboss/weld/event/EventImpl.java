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
package org.jboss.weld.event;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.builtin.AbstractFacade;
import org.jboss.weld.bean.builtin.FacadeInjectionPoint;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Formats;

import static org.jboss.weld.logging.messages.EventMessage.PROXY_REQUIRED;

/**
 * Implementation of the Event interface
 *
 * @param <T> The type of event being wrapped
 * @author David Allen
 * @see javax.enterprise.event.Event
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Uses SerializationProxy")
public class EventImpl<T> extends AbstractFacade<T, Event<T>> implements Event<T>, Serializable {

    private static final long serialVersionUID = 656782657242515455L;

    public static <E> EventImpl<E> of(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        return new EventImpl<E>(injectionPoint, beanManager);
    }

    private EventImpl(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        super(injectionPoint, null, beanManager);
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotations(getQualifiers()) + " Event<" + Formats.formatType(getType()) + ">";
    }

    public void fire(T event) {
        getBeanManager().getAccessibleObserverNotifier().fireEvent(getType(), event, getQualifiers());
    }

    public Event<T> select(Annotation... qualifiers) {
        return selectEvent(this.getType(), qualifiers);
    }

    public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        return selectEvent(subtype, qualifiers);
    }

    public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return selectEvent(subtype.getType(), qualifiers);
    }

    private <U extends T> Event<U> selectEvent(Type subtype, Annotation[] newQualifiers) {
        Observers.checkEventObjectType(getBeanManager(), subtype);
        return new EventImpl<U>(new FacadeInjectionPoint(getBeanManager().getContextId(), getInjectionPoint(), subtype, getQualifiers(), newQualifiers), getBeanManager());
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<T> extends AbstractFacadeSerializationProxy<T, Event<T>> {

        private static final long serialVersionUID = 9181171328831559650L;

        public SerializationProxy(EventImpl<T> event) {
            super(event);
        }

        private Object readResolve() {
            return EventImpl.of(getInjectionPoint(), getBeanManager());
        }

    }

}
