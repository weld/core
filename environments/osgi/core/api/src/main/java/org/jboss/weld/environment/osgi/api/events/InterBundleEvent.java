/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.api.events;

import javax.inject.Provider;

/**
 * <p>This class represents a communication event between bean bundles.</p>
 * <p>It allows to specify: <ul>
 * <li>
 * <p>The message as an {@link Object},</p>
 * </li>
 * <li>
 * <p>The type of the message as a {@link Class},</p>
 * </li>
 * <li>
 * <p>The origin of the message (within or outside the bundle).</p>
 * </li>
 * </ul></p>
 * <p>It may be used in {@link javax.enterprise.event.Observes} method in order
 * to listen inter-bundle communications.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see org.osgi.framework.Bundle
 * @see javax.enterprise.event.Observes
 * @see org.jboss.weld.environment.osgi.api.annotation.Sent
 */
public class InterBundleEvent {
    private final Object event;

    private boolean sent = false;

    private Class<?> type;

    /**
     * Construct a new {@link InterBundleEvent} with a message.
     *
     * @param event the message as an {@link Object}.
     */
    public InterBundleEvent(Object event) {
        this.event = event;
    }

    /**
     * Construct a new {@link InterBundleEvent} with a typed message.
     *
     * @param event the message as an {@link Object}.
     * @param type  the type of the message as a {@link Class}
     */
    public InterBundleEvent(Object event, Class<?> type) {
        this.event = event;
        this.type = type;
    }

    /**
     * Obtain the type of the message.
     *
     * @return the type of the message as a {@link Class}.
     */
    public Class<?> type() {
        if (type != null) {
            return type;
        } else {
            return event.getClass();
        }
    }

    /**
     * Test if the message type matches the given type.
     *
     * @param type the tested type as a {@link Class}
     * @return true if the given type matches the message type, false otherwise.
     */
    public boolean isTyped(Class<?> type) {
        if (this.type != null) {
            return this.type.equals(type);
        } else {
            return type.isAssignableFrom(event.getClass());
        }
    }

    /**
     * Obtain a parametrized provider if the given type matches the message type.
     *
     * @param type the wanted provider type as a {@link Class}.
     * @param <T>  the wanted provider type.
     * @return a {@link Provider} for the wanted type.
     * @throws {@link RuntimeException} if the wanted type does not match the message type.
     */
    public <T> Provider<T> typed(Class<T> type) {
        if (isTyped(type)) {
            return new Provider<T>() {
                @Override
                public T get() {
                    return (T) event;
                }

            };
        } else {
            throw new RuntimeException("The event is not of type " + type.getName());
        }
    }

    /**
     * Obtain the message.
     *
     * @return the message as an {@link Object}
     */
    public Object get() {
        return event;
    }

    /**
     * Test if the communication event comes from within or outside the current bundle.
     *
     * @return
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * Mark the communication event as sent outside the firing bundle.
     */
    public void sent() {
        this.sent = true;
    }

}
