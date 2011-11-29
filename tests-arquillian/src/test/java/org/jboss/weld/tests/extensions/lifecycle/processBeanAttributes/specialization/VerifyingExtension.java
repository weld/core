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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

public class VerifyingExtension implements Extension {

    private BeanAttributes<Alpha> alpha;
    private BeanAttributes<Bravo> bravo;
    private BeanAttributes<Charlie> charlie;

    public void alpha(@Observes ProcessBeanAttributes<Alpha> event) {
        Set<Type> types = event.getBeanAttributes().getTypes();
        if (!types.contains(Bravo.class) && !types.contains(Charlie.class)) {
            alpha = event.getBeanAttributes();
        }
    }

    public void bravo(@Observes ProcessBeanAttributes<Bravo> event) {
        Set<Type> types = event.getBeanAttributes().getTypes();
        if (!types.contains(Charlie.class)) {
            bravo = event.getBeanAttributes();
        }
    }

    public void charlie(@Observes ProcessBeanAttributes<Charlie> event) {
        if (event.getBeanAttributes().getTypes().contains(Charlie.class)) {
            charlie = event.getBeanAttributes();
        }
    }

    public BeanAttributes<Alpha> getAlpha() {
        return alpha;
    }

    public BeanAttributes<Bravo> getBravo() {
        return bravo;
    }

    public BeanAttributes<Charlie> getCharlie() {
        return charlie;
    }
}
