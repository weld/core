/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PassivatingContextTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(PassivatingContextTest.class))
                .addPackage(PassivatingContextTest.class.getPackage());
    }

    @Inject
    private BeanManagerImpl beanManager;

    /**
     * The built-in session and conversation scopes are passivating. No other
     * built-in scope is passivating.
     */
    @Test
    public void testIsSessionScopePassivating() {
        Assert.assertTrue(
                beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(SessionScoped.class).isPassivating());
    }

    /**
     * The built-in session and conversation scopes are passivating. No other
     * built-in scope is passivating.
     */
    @Test
    public void testIsConversationScopePassivating() {
        Assert.assertTrue(beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(ConversationScoped.class)
                .isPassivating());
    }

    /**
     * The built-in session and conversation scopes are passivating. No other
     * built-in scope is passivating.
     */
    @Test
    public void testIsApplicationScopeNonPassivating() {
        Assert.assertFalse(beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(ApplicationScoped.class)
                .isPassivating());
    }

    /**
     * The built-in session and conversation scopes are passivating. No other
     * built-in scope is passivating.
     */
    @Test
    public void testIsRequestScopeNonPassivating() {
        Assert.assertFalse(
                beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(RequestScoped.class).isPassivating());
    }

}
