/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.alternatives.weld2313;

import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Benjamin Confino
 */
@RunWith(Arquillian.class)
public class VetoedAlternativeWithoutPropertyTest {

    @Deployment @ShouldThrowException(DeploymentException.class)
    public static WebArchive createTestArchive() {
        BeansXml beansXml = new BeansXml().alternatives(MockPaymentProcessorImpl.class).stereotype(AlternativeConsumerStereotype.class);
        WebArchive testDeployment = ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(VetoedAlternativeWithoutPropertyTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(VetoedAlternativeWithoutPropertyTest.class.getPackage())
                .addAsServiceProvider(Extension.class, VetoingExtension.class)
                .addAsWebInfResource(beansXml, "beans.xml");
        return testDeployment;
    }

    @Test
    public void vetoedAlternativesShouldBeRejected() throws Exception {
        // should throw deployment exception
    }
}
