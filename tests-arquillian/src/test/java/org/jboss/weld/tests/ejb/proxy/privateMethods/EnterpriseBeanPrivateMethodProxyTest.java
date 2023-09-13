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
package org.jboss.weld.tests.ejb.proxy.privateMethods;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanPrivateMethodProxyTest {

    @Inject
    Foo foo;

    @Deployment
    public static EnterpriseArchive getDeployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class,
                Utils.getDeploymentNameAsHash(EnterpriseBeanPrivateMethodProxyTest.class,
                        Utils.ARCHIVE_TYPE.EAR));
        ear.addAsModule(Testable.archiveToTest(ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Foo.class, EnterpriseBeanPrivateMethodProxyTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")//.setManifest(new StringAsset("Manifest-Version: 1.0\nClass-Path: test-ejb.jar\n")))
        ));
        ear.addAsModule(ShrinkWrap.create(BeanArchive.class, "test-ejb1.jar")
                .addClasses(BeanWithPrivateMethod.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
        ear.addAsModule(ShrinkWrap.create(BeanArchive.class, "test-ejb2.jar")
                .addClasses(TestedBean.class, SingletonSessionBean.class, SFSessionBean.class, SLSessionBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
        ear.addAsResource(EnterpriseBeanPrivateMethodProxyTest.class.getPackage(), "jboss-deployment-structure.xml",
                "jboss-deployment-structure.xml");
        ear.addAsResource(EnterpriseBeanPrivateMethodProxyTest.class.getPackage(), "application.xml", "application.xml");
        return ear;
    }

    @Test
    public void testSingletonSessionBean() {
        Assert.assertTrue(foo.pingSingletonSessionBean());
    }

    @Test
    public void testStatefulSessionBean() {
        Assert.assertTrue(foo.pingStatefulSessionBean());
    }

    @Test
    public void testStatelessSessionBean() {
        Assert.assertTrue(foo.pingStatelessSessionBean());
    }
}
