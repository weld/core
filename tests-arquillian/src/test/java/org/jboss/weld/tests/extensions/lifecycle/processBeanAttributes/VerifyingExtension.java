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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

public class VerifyingExtension implements Extension {

    private BeanAttributes<Alpha> alpha;
    private BeanAttributes<Bravo> bravo;
    private BeanAttributes<Charlie> charlie;
    private BeanAttributes<Mike> mike;

    void observeAlpha(@Observes ProcessBeanAttributes<Alpha> event) {
        alpha = event.getBeanAttributes();
    }

    void observeBravo(@Observes ProcessBeanAttributes<Bravo> event) {
        bravo = event.getBeanAttributes();
    }

    void observeCharlie(@Observes ProcessBeanAttributes<Charlie> event) {
        charlie = event.getBeanAttributes();
    }

    void observeMike(@Observes ProcessBeanAttributes<Mike> event) {
        mike = event.getBeanAttributes();
    }

    protected BeanAttributes<Alpha> getAlpha() {
        return alpha;
    }

    protected BeanAttributes<Bravo> getBravo() {
        return bravo;
    }

    protected BeanAttributes<Charlie> getCharlie() {
        return charlie;
    }

    void vetoClassLevelBravoBean(@Observes ProcessAnnotatedType<Bravo> event) {
        event.veto();
    }

    void vetoClassLevelCharlieBean(@Observes ProcessAnnotatedType<Charlie> event) {
        event.veto();
    }

    public BeanAttributes<Mike> getMike() {
        return mike;
    }
}
