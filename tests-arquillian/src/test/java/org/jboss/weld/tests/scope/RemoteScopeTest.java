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
package org.jboss.weld.tests.scope;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import jakarta.servlet.http.HttpServletResponse;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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

@RunWith(Arquillian.class)
@Category(Integration.class)
public class RemoteScopeTest {

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(RemoteScopeTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Bar.class, Foo.class, RemoteClient.class, Special.class, Temp.class, TempConsumer.class,
                        TempProducer.class, Useless.class)
                .addClasses(Utils.class, Assert.class, Description.class, SelfDescribing.class, ComparisonFailure.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /*
     * description = "WELD-311"
     */
    @Test
    public void testScopeOfProducerMethod(@ArquillianResource URL baseURL) throws Exception {
        WebClient client = new WebClient();
        Page page = client.getPage(new URL(baseURL, "request1"));
        assertEquals(page.getWebResponse().getStatusCode(), HttpServletResponse.SC_OK);
        page = client.getPage(new URL(baseURL, "request2"));
        assertEquals(page.getWebResponse().getStatusCode(), HttpServletResponse.SC_OK);
    }

}
