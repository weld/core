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
package org.jboss.weld.tests.interceptors.aroundConstruct.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Instance;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AroundConstructTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(AlphaInterceptor.class, BravoInterceptor.class, CharlieInterceptor1.class, CharlieInterceptor2.class)
                .addPackage(AroundConstructTest.class.getPackage()).addClass(Utils.class);
    }

    @Test
    public void testInterceptorInvocation(Instance<Alpha> instance) {
        AlphaInterceptor.reset();
        instance.get();
        assertTrue(AlphaInterceptor.isInvoked());
    }

    @Test
    public void testReplacingParameters(Instance<Bravo> instance) {
        BravoInterceptor.reset();
        Bravo bravo = instance.get();
        assertNotNull(bravo.getParameter());
        assertEquals(BravoInterceptor.NEW_PARAMETER_VALUE, bravo.getParameter().getValue());
        assertTrue(BravoInterceptor.isInvoked());
    }

    @Test
    public void testExceptions(Instance<Charlie> instance) {
        CharlieInterceptor1.reset();
        CharlieInterceptor2.reset();
        try {
            instance.get();
            Assert.fail();
        } catch (CharlieException e) {
            assertTrue(CharlieInterceptor1.isInvoked());
            assertTrue(CharlieInterceptor2.isInvoked());
        } catch (Throwable e) {
            Assert.fail();
        }
    }

    @Test
    public void testSerialization(Instance<Alpha> instance) throws Exception {
        Utils.deserialize(Utils.serialize(instance.get()));
    }
}
