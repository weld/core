/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.signed;

import java.io.File;
import java.nio.file.Paths;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.proxy.signed.insideJar.Alpha;
import org.jboss.weld.tests.proxy.signed.insideJar.Beta;
import org.jboss.weld.tests.proxy.signed.insideJar.CharlieFace;
import org.jboss.weld.tests.proxy.signed.insideJar.CharlieImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * IMPORTANT: The classes under package "org.jboss.weld.tests.proxy.signed.insideJar" have been MANUALLY put into a JAR, which
 * was then self-signed and is attached to this test. E.g. the classes there are more or less to show what's inside the JAR, the
 * test operates on signed JAR.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ProxyCreationForSignedClassesTest {

    @Deployment
    public static WebArchive deploy() {
        // prepare file containing signed JAR, the path should be OS agnostic
        File signedJar = Paths
                .get("src", "test", "resources", "org", "jboss", "weld", "tests", "proxy", "signed", "signed-test.jar")
                .toFile();

        // WAR with singed JAR as a library
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(ProxyCreationForSignedClassesTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ProxyCreationForSignedClassesTest.class.getPackage())
                // add previously created signed JAR
                .addAsLibrary(signedJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    Alpha alpha; // simple bean

    @Inject
    Beta beta; // uses package private class

    @Inject
    CharlieFace charlie; //typed to interface only

    @Test
    public void testProxyCanBeCreatedForAllBeans() {
        Assert.assertNotNull(alpha);
        Assert.assertEquals(Alpha.class.getSimpleName(), alpha.ping());
        Assert.assertNotNull(beta);
        Assert.assertEquals("PackagePrivate", beta.ping());
        Assert.assertNotNull(charlie);
        Assert.assertEquals(CharlieImpl.class.getSimpleName(), charlie.ping());
    }
}
