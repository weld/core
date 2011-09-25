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
package org.jboss.weld.tests.activities.current;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

@RunWith(Arquillian.class)
public class NonNormalScopeTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(NonNormalScopeTest.class.getPackage())
                .addClass(Utils.class);
    }

    private static class DummyContext implements Context {

        private boolean active = true;

        public <T> T get(Contextual<T> contextual) {
            return null;
        }

        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            return null;
        }

        public Class<? extends Annotation> getScope() {
            return Dummy.class;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    }

    private static class NonNormalContext extends DummyContext {

        @Override
        public Class<? extends Annotation> getScope() {
            return NonNormalScope.class;
        }

    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test(expected = IllegalArgumentException.class)
    public void testNonNormalScope() {
        Context dummyContext = new NonNormalContext();
        beanManager.addContext(dummyContext);
        WeldManager childActivity = beanManager.createActivity();
        childActivity.setCurrent(dummyContext.getScope());
    }
}
