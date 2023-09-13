/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.metadata.anonymousAndLocalClass;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test requires PAT observes for all types and a local and anonymous class.In this case Weld will detect an observer for every
 * possible AT and hence attempt to create them. The point of this test is to verify that we do not create ATs from local and
 * anonymous classes.
 *
 * @see WELD-2498
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class NoMetadataForAnonymousAndLocalClassesTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(NoMetadataForAnonymousAndLocalClassesTest.class))
                .addPackage(NoMetadataForAnonymousAndLocalClassesTest.class.getPackage())
                .addAsServiceProvider(Extension.class, OmniseeingExtension.class);
    }

    @Test
    public void testNoATObserved() {
        Assert.assertEquals(0, OmniseeingExtension.ANONYMOUS_CLASS_OBSERVED);
        Assert.assertEquals(0, OmniseeingExtension.LOCAL_CLASS_OBSERVED);
    }
}
