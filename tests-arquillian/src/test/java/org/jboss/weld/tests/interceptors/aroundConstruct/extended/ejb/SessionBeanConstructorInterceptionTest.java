/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.aroundConstruct.extended.ejb;

import static org.junit.Assert.assertEquals;

import java.util.List;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class SessionBeanConstructorInterceptionTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SessionBeanConstructorInterceptionTest.class))
                .intercept(AlphaInterceptor1.class, AlphaInterceptor2.class, BravoInterceptor.class)
                .addPackage(SessionBeanConstructorInterceptionTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testConstructorLevelBinding(Instance<BeanWithConstructorLevelBinding> instance) {
        ActionSequence.reset();
        instance.get();
        assertSequenceEquals(AlphaInterceptor2.class, BeanWithConstructorLevelBinding.class);
    }

    @Test
    public void testTypeLevelBinding(Instance<BeanWithTypeLevelBinding> instance) {
        ActionSequence.reset();
        instance.get();
        assertSequenceEquals(AlphaInterceptor1.class, BeanWithTypeLevelBinding.class);
    }

    @Test
    public void testTypeLevelAndConstructorLevelBinding(Instance<BeanWithConstructorLevelAndTypeLevelBinding> instance) {
        ActionSequence.reset();
        instance.get();
        assertSequenceEquals(AlphaInterceptor1.class, BravoInterceptor.class,
                BeanWithConstructorLevelAndTypeLevelBinding.class);
    }

    @Test
    public void testOverridingTypeLevelBinding(Instance<BeanOverridingTypeLevelBinding> instance) {
        ActionSequence.reset();
        instance.get();
        assertSequenceEquals(AlphaInterceptor2.class, BeanOverridingTypeLevelBinding.class);
    }

    private void assertSequenceEquals(Class<?>... expected) {
        List<String> data = ActionSequence.getSequence().getData();
        assertEquals(expected.length, data.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getSimpleName(), data.get(i));
        }
    }
}
