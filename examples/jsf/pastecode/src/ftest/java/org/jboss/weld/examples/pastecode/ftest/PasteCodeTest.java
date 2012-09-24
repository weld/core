/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.pastecode.ftest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.ajocado.dom.Attribute;
import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.attribute.AttributeLocator;
import org.jboss.arquillian.ajocado.locator.option.OptionLabelLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.ajocado.locator.option.OptionLocatorFactory.optionLabel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests PasteCode example using selenium framework. Furthermore this test
 * is used as a basis for performance tests of Weld. A SmartFrog component is generated
 * from this test through Smartfrog-Sniff and used for measuring availability, scalability,
 * response times and other parameters related to performance.
 *
 * @author maschmid
 * @author mgencur
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PasteCodeTest {
    protected String MAIN_PAGE = "home.jsf";

    //main page elements
    protected XPathLocator SUBMIT_BUTTON = xp("//input[contains(@src,'img/submit.png')]");
    protected XPathLocator NEW_LINK = xp("//a[contains(text(),'new')]");
    protected XPathLocator HISTORY_LINK = xp("//a[contains(text(),'history')]");
    protected XPathLocator HELP_LINK = xp("//a[contains(text(),'help')]");
    protected XPathLocator POST_AREA = xp("//textarea[contains(@class,'pastecode')]");
    protected XPathLocator SYNTAX_SELECT = xp("//select[contains(@id,'language')]");
    protected XPathLocator EXPOSURE_SELECT = xp("//select[contains(@id,'exposure')]");
    protected OptionLabelLocator ANY_SYNTAX = optionLabel("Any");
    protected OptionLabelLocator JS_SYNTAX = optionLabel("JavaScript");
    protected OptionLabelLocator PRIVATE_EXPOSURE = optionLabel("Private");
    protected XPathLocator NAME_INPUT = xp("//input[contains(@id,'user')]");
    //resulting page after new post submitting
    protected XPathLocator DOWNLOAD_LINK = xp("//a[contains(text(),'DOWNLOAD')]");
    protected XPathLocator PUBLIC_TESTER_LINK = xp("//div[contains(@class,'recentPaste')]/a[contains(text(),'PublicTester')]");
    protected XPathLocator PRIVATE_TESTER_LINK = xp("//div[contains(@class,'recentPaste')]/a[contains(text(),'PrivateTester')]");
    //TODO: change Plain text to JavaScript when it's possible to choose it during posting of code-fragment
    protected XPathLocator JS_TEXT = xp("//span[contains(@class,'recentPasteLang')][contains(text(),'JavaScript')]");
    //recent posts test elements
    protected XPathLocator CRAZYMAN_LINK = xp("//a[contains(text(),'crazyman')][contains(@href,'24')]");
    //history page elements
    protected XPathLocator USER_SEARCH_INPUT = xp("//input[contains(@name,'user')]");
    protected XPathLocator DATE_SEARCH_INPUT = xp("//input[contains(@name,'pasteDate')]");
    protected XPathLocator CODE_SEARCH_INPUT = xp("//textarea[contains(@name,'code')]");
    protected XPathLocator SEARCH_BUTTON = xp("//input[contains(@src,'img/search.png')]");
    protected XPathLocator ACTIVE_FIRST_PAGE_LINK = xp("//span[contains(@class,'currentPage')][contains(text(),'1')]");
    protected XPathLocator SECOND_PAGE_LINK = xp("//a[contains(@class,'pagination')][contains(text(),'2')]");
    protected XPathLocator THIRD_PAGE_LINK = xp("//a[contains(@class,'pagination')][contains(text(),'3')]");

    @Drone
    AjaxSelenium selenium;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Before
    public void openStartURL() throws MalformedURLException {
        // selenium.setSpeed(200);
        selenium.open(new URL(contextPath.toString() + MAIN_PAGE));
    }

    @Test
    @InSequence(1)
    public void mainPageTest() {
        assertTrue("A page should contain text 'new'", selenium.isTextPresent("new"));
        assertTrue("A page should contain text 'martin'", selenium.isTextPresent("martin"));
        assertTrue("A page should contain text '6 Feb'", selenium.isTextPresent("6 Feb"));
        assertTrue("A page should contain text 'Exposure'", selenium.isTextPresent("Exposure"));
    }

    @Test
    @InSequence(2)
    public void newPublicPostTest() {        
        selenium.type(POST_AREA, CODE_FRAGMENT);
        selenium.select(SYNTAX_SELECT, JS_SYNTAX);
        selenium.type(NAME_INPUT, "PublicTester");
        waitForHttp(selenium).click(SUBMIT_BUTTON);

        assertTrue("Location is not correct", isLocationCorrect(selenium.getLocation(), false));

        assertTrue("A page should contain text 'Posted by PublicTester just now'", selenium.isTextPresent("Posted by PublicTester just now"));
        assertTrue("A page should contain pasted text", selenium.isTextPresent(CODE_FRAGMENT_PART));
        assertTrue("A page should contain element 'DOWNLOAD'", selenium.isElementPresent(DOWNLOAD_LINK));

        assertTrue("It should be able to download file from database", isDownloadWorking(selenium, DOWNLOAD_LINK, CODE_FRAGMENT_PART));

        assertTrue("A page should contain element 'Tester'", selenium.isElementPresent(PUBLIC_TESTER_LINK));
        assertTrue("A page should contain element 'JavaScript'", selenium.isElementPresent(JS_TEXT));
    }

    @Test
    @InSequence(3)
    public void newPrivatePostTest() {
        selenium.type(POST_AREA, CODE_FRAGMENT);
        selenium.select(SYNTAX_SELECT, JS_SYNTAX);
        selenium.select(EXPOSURE_SELECT, PRIVATE_EXPOSURE);
        selenium.type(NAME_INPUT, "PrivateTester");
        waitForHttp(selenium).click(SUBMIT_BUTTON);

        assertTrue("Location is not correct", isLocationCorrect(selenium.getLocation(), true));
        assertTrue("A page should contain text 'Posted by PrivateTester just now'", selenium.isTextPresent("Posted by PrivateTester just now"));
        assertTrue("A page should contain pasted text", selenium.isTextPresent(CODE_FRAGMENT_PART));
        assertTrue("A page should contain element 'DOWNLOAD'", selenium.isElementPresent(DOWNLOAD_LINK));
        assertFalse("A page shouldn't contain element 'PrivateTester'", selenium.isElementPresent(PRIVATE_TESTER_LINK));
    }


    @Test
    @InSequence(4)
    public void recentPostsTest() {
        waitForHttp(selenium).click(CRAZYMAN_LINK);
        assertTrue("A page should contain 'Posted by crazyman on 19 Feb'", selenium.isTextPresent("Posted by crazyman on 19 Feb"));
        assertTrue("A page should contain code fragment", selenium.isTextPresent("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
    }

    @Test
    @InSequence(5)
    public void exactSearchTest() {
        waitForHttp(selenium).click(HISTORY_LINK);
        assertTrue("A page should contain 'Posted by PublicTester'", selenium.isTextPresent("Posted by PublicTester"));
        assertFalse("A page shouldn't contain 'Posted by PrivateTester'", selenium.isTextPresent("Posted by PrivateTester"));

        selenium.type(USER_SEARCH_INPUT, "graham");
        selenium.type(DATE_SEARCH_INPUT, "2009-02-02");
        selenium.select(SYNTAX_SELECT, JS_SYNTAX);
        selenium.type(CODE_SEARCH_INPUT, "toggle_visibility(id)");
        waitForHttp(selenium).click(SEARCH_BUTTON);

        assertTrue("A page should contain 'Posted by graham ...'", selenium.isTextPresent("Posted by graham on 3 Feb 2009"));
        assertTrue("A page should contain 'Language: JavaScript'", selenium.isTextPresent("Language: JavaScript"));
        assertTrue("A page should contain the code fragment found", selenium.isTextPresent("var e = document.ge"));
        assertFalse(selenium.isElementPresent(ACTIVE_FIRST_PAGE_LINK)); //assert that only one record was found
    }

    @Test
    @InSequence(6)
    public void searchAndPaginationTest() {
        waitForHttp(selenium).click(HISTORY_LINK);
        selenium.type(USER_SEARCH_INPUT, "martin");
        selenium.select(SYNTAX_SELECT, ANY_SYNTAX);
        selenium.type(DATE_SEARCH_INPUT, "");
        selenium.type(CODE_SEARCH_INPUT, "");
        waitForHttp(selenium).click(SEARCH_BUTTON);
        assertTrue("A page should contain 'Posted by martin ...'", selenium.isTextPresent("Posted by martin on 15 Feb 2009"));
        waitForHttp(selenium).click(SECOND_PAGE_LINK);
        assertTrue("A page should contain 'Posted by martin ...'", selenium.isTextPresent("Posted by martin on 1 Feb 2009"));
        waitForHttp(selenium).click(THIRD_PAGE_LINK);
        assertTrue("A page should contain 'Posted by martin ...'", selenium.isTextPresent("Posted by martin on 9 Jan 2009"));
        assertTrue("A page should contain code fragment", selenium.isTextPresent("function build_calendar($month,$year,$dateArray)"));
    }

    @Test
    @InSequence(7)
    public void helpPageTest() {
        waitForHttp(selenium).click(HELP_LINK);
        assertTrue("A page should contain help information", selenium.isTextPresent("Useful Information"));
        assertTrue("A page should contain 'Weld Features Covered'", selenium.isTextPresent("Weld Features Covered"));
    }

    private boolean isLocationCorrect(URL url, boolean privatePost) {
        String regexp;

        if (!privatePost) {
            regexp = "[0-9]{1,}";
        } else {
            regexp = "[a-z0-9]{6}";
        }

        return url.getPath().substring("/weld-pastecode/".length()).matches(regexp);
    }

    private boolean isDownloadWorking(AjaxSelenium s, XPathLocator xp, String textToFind) {
        AttributeLocator<XPathLocator> al = xp.getAttribute(Attribute.HREF);
        try {
            URL downloadUrl = new URL(contextPath + s.getAttribute(al));
            BufferedReader r = new BufferedReader(new InputStreamReader(downloadUrl.openStream()));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString().contains(textToFind);
        } catch (IOException e) {
            return false;
        }
    }

    protected String CODE_FRAGMENT =
            "function addleadingzeros(array) {" + "\n" +
                    "var highestint = Math.max.apply(Math,array);" + "\n" +
                    "for(var i=0; i<array.length; i++) {" + "\n" +
                    "var nleadingzeros = highestint.toString().length - array[i].toString().length;" + "\n" +
                    "for(var j=0; j<nleadingzeros; j++) array[i] = '0' + array[i];" + "\n" +
                    "}" + "\n" +
                    "return array;" + "\n" +
                    "}";

    protected String CODE_FRAGMENT_PART = CODE_FRAGMENT.substring(0, 30);
}
