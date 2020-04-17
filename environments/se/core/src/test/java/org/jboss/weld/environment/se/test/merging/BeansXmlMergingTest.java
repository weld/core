/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.merging;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.jboss.weld.tests.util.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simulates a situation where there is implicit discovery (jakarta.enterprise.inject.scan.implicit)
 * and no org.jboss.weld.se.archive.isolation and there are multiple archives on the classpath.
 * This triggers beans.xml merging.
 *
 * The test controls deployment flow so as to be able to set system variables.
 */
@RunWith(Arquillian.class)
public class BeansXmlMergingTest {

    private static final String isolationOriginalValue = System.getProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);
    private static final String implicitScanOriginalValue = System.getProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT);

    @Deployment(managed = false)
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);
        JavaArchive archive01 = ShrinkWrap
                .create(BeanArchive.class)
                // no beans.xml here but still should be discovered
                .addClasses(Foo.class);
        JavaArchive archive02 = ShrinkWrap
                .create(BeanArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL),
                        "beans.xml")
                .addClasses(Bar.class);
        archives.add(archive01);
        archives.add(archive02);
        return archives;
    }

    @ArquillianResource
    private Deployer deployer;

    @Before
    public void before() {
        System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, Boolean.toString(false));
        System.setProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, Boolean.toString(true));
        deployer.deploy("_DEFAULT_");
    }

    @After
    public void after() {
        deployer.undeploy("_DEFAULT_");
    }

    @AfterClass
    public static void setIsolationBackToOriginal() {
        if (isolationOriginalValue == null) {
            System.clearProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);
        } else {
            System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, isolationOriginalValue);
        }
        if (implicitScanOriginalValue == null) {
            System.clearProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT);
        } else {
            System.setProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, implicitScanOriginalValue);
        }
    }

    @Test
    public void testArchivesCanBeDeployedAndDiscoveryWorks(Foo foo, Bar bar) {
        Assert.assertNotNull(foo);
        Assert.assertNotNull(bar);
    }
}