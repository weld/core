/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.servlet.test.deployment.bda.additional;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldDeployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1832
 */
@RunWith(Arquillian.class)
public class AdditionalBeanDeploymentArchiveTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment()
                .addAsWebInfResource(new ByteArrayAsset(SimpleExtension.class.getName().getBytes()),
                        "classes/META-INF/services/" + Extension.class.getName())
                .addClasses(AdditionalBeanDeploymentArchiveTest.class, SimpleExtension.class)
                .addAsLibraries(ShrinkWrap.create(BeanArchive.class).addClass(Dummy.class),
                        ShrinkWrap.create(JavaArchive.class).addClass(Outsider.class));
    }

    @Test
    public void testAdditionalBeanDeploymentArchiveCreated(Outsider outsider, BeanManagerImpl beanManager) {
        assertNotNull(outsider);
        outsider.ping();
        Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchivesMap = Container.instance(beanManager)
                .beanDeploymentArchives();
        assertEquals(3, beanDeploymentArchivesMap.size());
        boolean additionalBdaFound = false;
        for (BeanDeploymentArchive bda : beanDeploymentArchivesMap.keySet()) {
            if (bda.getId().equals(WeldDeployment.ADDITIONAL_BDA_ID)) {
                additionalBdaFound = true;
                break;
            }
        }
        assertTrue(additionalBdaFound);
    }

}
