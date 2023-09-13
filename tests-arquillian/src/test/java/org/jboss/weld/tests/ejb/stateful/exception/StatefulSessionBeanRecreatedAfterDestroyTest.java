/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.ejb.stateful.exception;

import static org.junit.Assert.assertEquals;

import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @see https://community.jboss.org/message/772357#772357
 * @see https://issues.jboss.org/browse/WELD-1254
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class StatefulSessionBeanRecreatedAfterDestroyTest {

    @Inject
    private StatefulBean bean;

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(StatefulSessionBeanRecreatedAfterDestroyTest.class))
                .addPackage(StatefulSessionBeanRecreatedAfterDestroyTest.class.getPackage());
    }

    @Test
    public void testWithSpi() {
        assertEquals("pong", bean.ping());

        // cause SFSB to be removed
        try {
            bean.throwException();
            Assert.fail();
        } catch (EJBException expected) {
        }

        // verify that every access causes NoSuchEJBException
        try {
            bean.ping();
            Assert.fail();
        } catch (NoSuchEJBException expected) {
        }

        // try to destroy the bean
        Bean<?> contextual = manager.resolve(manager.getBeans(StatefulBean.class));
        AlterableContext context = (AlterableContext) manager.getContext(ApplicationScoped.class);
        context.destroy(contextual);

        assertEquals("pong", bean.ping());
    }

    @Test
    public void testWithInstance() {
        assertEquals("pong", bean.ping());

        // cause SFSB to be removed
        try {
            bean.throwException();
            Assert.fail();
        } catch (EJBException expected) {
        }

        // verify that every access causes NoSuchEJBException
        try {
            bean.ping();
            Assert.fail();
        } catch (NoSuchEJBException expected) {
        }

        CDI.current().destroy(bean);

        assertEquals("pong", bean.ping());
    }
}
