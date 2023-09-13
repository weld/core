/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.finalMethod;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InterceptedBeanWithFinalMethodTest {

    @Inject
    private TopSecretBriefing briefing;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptedBeanWithFinalMethodTest.class))
                .intercept(TopSecretInterceptor.class).addPackage(InterceptedBeanWithFinalMethodTest.class.getPackage());
    }

    // WELD-769
    @Test
    public void testInterceptionWorksOnClassWithFinalMethod() {
        Assert.assertEquals(TopSecretBriefing.MESSAGE + TopSecretInterceptor.MESSAGE, briefing.performBriefing());
    }

    /*
     * description = "WELD-771"
     */
    @Test
    public void testFinalMethodInvocationOnInterceptedBean() {
        briefing.performBriefing();
        Assert.assertTrue(briefing.isBriefingPerformed());
    }
}
