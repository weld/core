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
package org.jboss.weld.event;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * An implementation of {@link ObserverMethod} used for events delivered to extensions. An event can obtain an information about the
 * observer method and receiver used for event delivery using {@link AbstractContainerEvent#getObserverMethod()} or
 * {@link AbstractContainerEvent#getReceiver()}, respectively. The observer method does not require contexts to be active.
 *
 * @author Jozef Hartinger
 *
 */
public class ExtensionObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X> {

    protected ExtensionObserverMethodImpl(WeldMethod<T, ? super X> observer, RIBean<X> declaringBean, BeanManagerImpl manager) {
        super(observer, declaringBean, manager);
    }

    @Override
    protected void preNotify(T event, Object receiver) {
        if (event instanceof AbstractContainerEvent) {
            setNotificationContext((AbstractContainerEvent) event, this, receiver);
        }
    }

    @Override
    protected void postNotify(T event, Object receiver) {
        if (event instanceof AbstractContainerEvent) {
            setNotificationContext((AbstractContainerEvent) event, null, null);
        }
    }

    private void setNotificationContext(AbstractContainerEvent event, ObserverMethod<?> observer, Object receiver) {
        event.setObserverMethod(observer);
        event.setReceiver(receiver);
    }

    @Override
    protected Object getReceiverIfExists() {
        return getReceiver(null);
    }

    /*
     * Contexts may not be active during notification of container lifecycle events. Therefore, we invoke the methods direcly on
     * an extension instance.
     */
    @Override
    protected Object getReceiver(CreationalContext<?> ctx) {
        return getDeclaringBean().create(null);
    }
}
