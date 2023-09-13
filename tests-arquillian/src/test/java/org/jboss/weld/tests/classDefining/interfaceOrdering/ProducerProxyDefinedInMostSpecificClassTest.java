/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.classDefining.interfaceOrdering;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.classDefining.interfaceOrdering.a.A;
import org.jboss.weld.tests.classDefining.interfaceOrdering.a.One;
import org.jboss.weld.tests.classDefining.interfaceOrdering.b.B;
import org.jboss.weld.tests.classDefining.interfaceOrdering.b.Two;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that a proxy for a producer-created bean starts with the package and class of the most concrete class/interface.
 * This should prevent problems in JPMS where each class/interface can come from various module that don't have
 * bidirectional read between them.
 */
@RunWith(Arquillian.class)
public class ProducerProxyDefinedInMostSpecificClassTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducerProxyDefinedInMostSpecificClassTest.class))
                .addClass(ProducerProxyDefinedInMostSpecificClassTest.class)
                .addClass(A.class)
                .addClass(B.class)
                .addClass(One.class)
                .addClass(Two.class)
                .addClass(ProducerBean.class);
    }

    @Inject
    B someB;

    @Inject
    Two two;

    @Test
    public void testProxyNameForInterfaceBasedBean() {
        // first test that bean can be used
        someB.pingB();
        // then assert its name up to the first occurrence of "$"
        // in this case, we want the package and class name to the that of B which is the most specific interface implemented
        Assert.assertEquals(B.class.getName(),
                someB.getClass().getName().substring(0, someB.getClass().getName().indexOf("$")));
    }

    @Test
    public void testProxyNameForClassBasedBean() {
        // first test that bean can be used
        two.pingTwo();
        // then assert its name up to the first occurrence of "$"
        // in this case, we want the package and class name to the that of Two which is the most specific class of the bean
        Assert.assertEquals(Two.class.getName(), two.getClass().getName().substring(0, two.getClass().getName().indexOf("$")));
    }
}
