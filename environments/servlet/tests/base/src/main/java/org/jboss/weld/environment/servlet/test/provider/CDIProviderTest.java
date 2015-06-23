/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.servlet.test.provider;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matus Abaffy
 */
@RunWith(Arquillian.class)
public class CDIProviderTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment().addPackage(CDIProviderTest.class.getPackage());
    }

    @Test
    public void testCDIProvider() {
        BeanManager manager = KarateClubLocator.getBeanManager();
        assertNotNull(manager);
        // Girl, Chick
        assertEquals(2, manager.getBeans(Child.class, new AnnotationLiteral<Female>() {
            private static final long serialVersionUID = 1L;
        }).size());
        // Chick
        assertEquals(1, manager.getBeans(Girl.class, new AnnotationLiteral<Pretty>() {
            private static final long serialVersionUID = 2L;
        }).size());

        KarateClub club = KarateClubLocator.lookupKarateClub();
        assertNotNull(club);
        assertTrue(club.kick());
    }
}
