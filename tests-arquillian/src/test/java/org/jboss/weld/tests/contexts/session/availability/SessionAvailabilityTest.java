/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.session.availability;

import java.io.IOException;
import java.net.URL;
import com.gargoylesoftware.htmlunit.WebClient;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class SessionAvailabilityTest {

    @ArquillianResource
    URL url;

    private final WebClient client = new WebClient();

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(WebArchive.class,
                Utils.getDeploymentNameAsHash(SessionAvailabilityTest.class, Utils.ARCHIVE_TYPE.WAR)).addPackage(SessionAvailabilityTest.class.getPackage())
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws IOException {
        // set data to session - increment int
        String id1 = pageAsString("set=true");
        //change session id 
        String id2 = pageAsString("change=true");
        Assert.assertNotSame(id1, id2);
        String p = pageAsString("print=true");
        Assert.assertEquals(p, "1");
    }

    private String pageAsString(String param) throws IOException {
        return client.getPage(url.toString() + "/test?" + param).getWebResponse().getContentAsString();
    }
}
