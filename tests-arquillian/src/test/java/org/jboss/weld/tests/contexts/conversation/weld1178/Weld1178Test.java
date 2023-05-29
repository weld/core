/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.weld1178;

import java.net.URL;

import com.gargoylesoftware.htmlunit.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class Weld1178Test {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(Weld1178Test.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(DefectiveBean.class, MyExceptionHandler.class, MyExceptionHandlerFactory.class)
                .addAsWebInfResource(currentPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(currentPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebResource(currentPackage(), "exceptionInRenderResponse.xhtml", "exceptionInRenderResponse.xhtml")
                .addAsWebResource(currentPackage(), "error.xhtml", "error.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static Package currentPackage() {
        return Weld1178Test.class.getPackage();
    }

    @Test
    public void testExceptionInRenderResponse() throws Exception {
        WebClient client = new WebClient();
        client.getPage(getPath("/exceptionInRenderResponse.xhtml"));
    }

    protected String getPath(String page) {
        return url + page;
    }

}
