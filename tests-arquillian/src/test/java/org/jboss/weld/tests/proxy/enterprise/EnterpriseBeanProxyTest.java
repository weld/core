/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.enterprise;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EnterpriseBeanProxyTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(EnterpriseBeanProxyTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModule(
                        ShrinkWrap.create(JavaArchive.class)
                                .addPackage(EnterpriseBeanProxyTest.class.getPackage())
                                .addClass(Utils.class)
                                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                                .addClass(Utils.class));
    }

    /*
     * description = "WBRI-109"
     *
     * <a href="https://jira.jboss.org/jira/browse/WBRI-109">WBRI-109</a>
     */
    // WELDINT-45
    @Test
    public void testNoInterfaceView(Mouse mouse) throws Exception {
        Assert.assertTrue(Utils.isProxy(mouse));
        Assert.assertTrue(mouse instanceof Mouse);
    }

}
