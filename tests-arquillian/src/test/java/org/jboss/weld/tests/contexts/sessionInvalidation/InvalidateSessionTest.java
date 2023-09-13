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

package org.jboss.weld.tests.contexts.sessionInvalidation;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * <p>
 * Check what happens when session.invalidate() is called.
 * </p>
 *
 * @author Pete Muir
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class InvalidateSessionTest {
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(InvalidateSessionTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Storm.class, SomeBean.class)
                .addAsWebInfResource(InvalidateSessionTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(InvalidateSessionTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebResource(InvalidateSessionTest.class.getPackage(), "storm.xhtml", "storm.xhtml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL url;

    /*
     * description = "WELD-380, WELD-403"
     */
    @Test
    public void testInvalidateSessionCalled() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        HtmlPage page = client.getPage(getPath("/storm.xhtml"));
        HtmlSubmitInput invalidateSessionButton = getFirstMatchingElement(page, HtmlSubmitInput.class,
                "invalidateSessionButton");
        page = invalidateSessionButton.click();
        HtmlInput inputField = getFirstMatchingElement(page, HtmlInput.class, "prop");
        Assert.assertEquals(Storm.PROPERTY_VALUE, inputField.getValueAttribute());

        // Make another request to verify that the session bean value is not the
        // one from the previous invalidated session.
        page = client.getPage(getPath("/storm.xhtml"));
        inputField = getFirstMatchingElement(page, HtmlInput.class, "prop");
        Assert.assertEquals(SomeBean.DEFAULT_PROPERTY_VALUE, inputField.getValueAttribute());
    }

    /*
     * description = "WELD-461"
     */
    @Test
    public void testNoDoubleDestructionOnExternalRedirect() throws Exception {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(getPath("/storm.xhtml"));
        HtmlSubmitInput button = getFirstMatchingElement(page, HtmlSubmitInput.class, "redirectButton");
        button.click();
    }

    protected String getPath(String page) {
        return url + page;
    }

    protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass) {
        Set<T> result = new HashSet<T>();

        for (HtmlElement element : rootElement.getHtmlElementDescendants()) {
            result.addAll(getElements(element, elementClass));
        }

        if (elementClass.isInstance(rootElement)) {
            result.add(elementClass.cast(rootElement));
        }
        return result;

    }

    protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id) {

        Set<T> inputs = getElements(page.getBody(), elementClass);
        for (T input : inputs) {
            if (input.getId().contains(id)) {
                return input;
            }
        }
        return null;
    }

}
