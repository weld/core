/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.alternatives.accessible;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class AccessibleAlternativesTest {

    private static StringAsset beansXml = new StringAsset(
            "<beans>"
                    + " <alternatives>"
                    + "   <class>" + BUser.class.getName() + "</class>"
                    + " </alternatives>"
                    + "</beans>");

    @Deployment
    public static Archive<?> deploy() {
        JavaArchive lib = ShrinkWrap.create(BeanArchive.class)
                .addClasses(IUser.class, BUser.class);

        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(AccessibleAlternativesTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsLibrary(lib)
                .addAsWebInfResource(beansXml, "beans.xml")
                .addClasses(AUser.class);
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testAlternatives() {
        Assert.assertEquals(BUser.class, beanManager.resolve(beanManager.getBeans(IUser.class)).getBeanClass());
    }
}
