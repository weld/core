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
package org.jboss.weld.tests.enterprise.lifecycle;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import jakarta.servlet.http.HttpServletResponse;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Sections
 * <p/>
 * 6.5. Lifecycle of stateful session beans 6.6. Lifecycle of stateless session
 * and singleton beans 6.11. Lifecycle of EJBs
 * <p/>
 * Mostly overlapping with other tests...
 *
 * @author Nicklas Karlsson
 * @author David Allen
 *         <p/>
 *         Spec version: Public Release Draft 2
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseBeanLifecycleRemoteTest {
    @Deployment(testable = false)
    public static Archive<?> deploy() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class,
                Utils.getDeploymentNameAsHash(EnterpriseBeanLifecycleRemoteTest.class,
                        Utils.ARCHIVE_TYPE.EAR));
        ear.addAsModule(ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RemoteClient.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .setManifest(new StringAsset("Manifest-Version: 1.0\nClass-Path: test-archive.jar\n")));
        ear.addAsModule(ShrinkWrap.create(BeanArchive.class, "test-archive.jar")
                .addClasses(KleinStadt.class, Kassel.class, GrossStadt.class, FrankfurtAmMain.class, SchoeneStadt.class)
                .addClasses(Utils.class, Assert.class, Description.class, SelfDescribing.class, ComparisonFailure.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
        return ear;
    }

    @ArquillianResource
    private URL url;

    @Test
    public void testDestroyRemovesSFSB() throws Exception {
        WebClient client = new WebClient();
        Page page = client.getPage(getPath("request1"));
        assertEquals(page.getWebResponse().getStatusCode(), HttpServletResponse.SC_OK);
        page = client.getPage(getPath("request2"));
        assertEquals(page.getWebResponse().getStatusCode(), HttpServletResponse.SC_OK);
    }

    protected String getPath(String viewId) {
        return url + viewId;
    }

}
