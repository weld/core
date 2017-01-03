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
package org.jboss.weld.tests.managed.newBean.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see WELD-975
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AnotherProgrammaticLookupOfNewBeanTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AnotherProgrammaticLookupOfNewBeanTest.class)).addClasses(PaymentProcessor.class, ChequePaymentProcessor.class, InjectedBean2.class);
    }

    @Test
    public void testProgrammaticLookupOfNewBean(InjectedBean2 bean, BeanManager manager) {
        assertEquals(1, manager.getBeans(ChequePaymentProcessor.class, New.Literal.INSTANCE).size());
        Instance<PaymentProcessor> instance = bean.getInstance();
        assertNotNull(instance);
        assertFalse(instance.isAmbiguous());
        assertFalse(instance.isUnsatisfied());

        PaymentProcessor processor1 = instance.get();
        assertTrue(processor1 instanceof PaymentProcessor);
        assertTrue(processor1 instanceof ChequePaymentProcessor);
        assertEquals("foo", processor1.getValue());
        processor1.setValue("bar");

        // verify that this is a @New bean and not the @ApplicationScoped one
        PaymentProcessor processor2 = instance.get();
        assertEquals("foo", processor2.getValue());
    }
}
