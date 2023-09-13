/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.ejb.duplicatenames;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @see WELD-1680
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class DuplicateEJBNamesDiscoveryTest {

    @Deployment(testable = true)
    public static EnterpriseArchive deploy() {

        JavaArchive jar1 = ShrinkWrap.create(JavaArchive.class, "first.jar")
                .addClasses(org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl.class);

        JavaArchive jar2 = ShrinkWrap.create(JavaArchive.class, "second.jar")
                .addClasses(org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl.class);

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war")
                .setManifest(new StringAsset("Manifest-Version: 1.0\nClass-Path: first.jar second.jar"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DuplicateEJBNamesDiscoveryTest.class);

        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(DuplicateEJBNamesDiscoveryTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(jar1, jar2)
                .addAsModule(Testable.archiveToTest(testWar));
    }

    @Inject
    private org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl first;

    @Inject
    private org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl second;

    @Test
    public void test() {
        Assert.assertEquals(org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl.MESSAGE, first.call());
        Assert.assertEquals(org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl.MESSAGE, second.call());
    }

}
