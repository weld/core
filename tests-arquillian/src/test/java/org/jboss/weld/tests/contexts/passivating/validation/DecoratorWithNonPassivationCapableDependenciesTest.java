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
package org.jboss.weld.tests.contexts.passivating.validation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.Collections;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that a decorator that is passivation capable while having non-passivation capable dependencies is allowed
 * provided it does not intercept a bean declaring passivation scope.
 * 
 * @author Jozef Hartinger
 * 
 */
@RunWith(Arquillian.class)
public class DecoratorWithNonPassivationCapableDependenciesTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).decorate(VesselDecorator.class)
                .addClasses(Engine.class, Ferry.class, Vessel.class, VesselDecorator.class);
    }

    @Test
    public void testDeploymentValid() {
        // it is enough to verify that the deployment passes validation and deploys
        assertEquals(1, manager.resolveDecorators(Collections.<Type> singleton(Ferry.class)).size());
    }
}
