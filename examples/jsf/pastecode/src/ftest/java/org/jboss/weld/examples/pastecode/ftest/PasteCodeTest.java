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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import static org.jboss.arquillian.graphene.Graphene.element;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * This class tests PasteCode example using selenium framework. Furthermore this
 * test is used as a basis for performance tests of Weld. A SmartFrog component
 * is generated from this test through Smartfrog-Sniff and used for measuring
 * availability, scalability, response times and other parameters related to
 * performance.
 *
 * @author maschmid
 * @author mgencur
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PasteCodeTest {

    protected String MAIN_PAGE = "home.jsf";
    //main page elements
    protected By SUBMIT_BUTTON = By.xpath("//input[contains(@src,'img/submit.png')]");
    protected By NEW_LINK = By.xpath("//a[contains(text(),'new')]");
    protected By HISTORY_LINK = By.xpath("//a[contains(text(),'history')]");
    protected By HELP_LINK = By.xpath("//a[contains(text(),'help')]");
    protected By POST_AREA = By.xpath("//textarea[contains(@class,'pastecode')]");
    protected By SYNTAX_SELECT = By.xpath("//select[contains(@id,'language')]");
    protected By EXPOSURE_SELECT = By.xpath("//select[contains(@id,'exposure')]");
    protected String ANY_SYNTAX = "Any";
    protected String JS_SYNTAX = "JavaScript";
    protected String PRIVATE_EXPOSURE = "Private";
    protected By NAME_INPUT = By.xpath("//input[contains(@id,'user')]");
    //resulting page after new post submitting
    protected By DOWNLOAD_LINK = By.xpath("//a[contains(text(),'DOWNLOAD')]");
    protected By PUBLIC_TESTER_LINK = By.xpath("//div[contains(@class,'recentPaste')]/a[contains(text(),'PublicTester')]");
    protected By PRIVATE_TESTER_LINK = By.xpath("//div[contains(@class,'recentPaste')]/a[contains(text(),'PrivateTester')]");
    //TODO: change Plain text to JavaScript when it's possible to choose it during posting of code-fragment
    protected By JS_TEXT = By.xpath("//span[contains(@class,'recentPasteLang')][contains(text(),'JavaScript')]");
    //recent posts test elements
    protected By CRAZYMAN_LINK = By.xpath("//a[contains(text(),'crazyman')][contains(@href,'24')]");
    //history page elements
    protected By USER_SEARCH_INPUT = By.xpath("//input[contains(@name,'user')]");
    protected By DATE_SEARCH_INPUT = By.xpath("//input[contains(@name,'pasteDate')]");
    protected By CODE_SEARCH_INPUT = By.xpath("//textarea[contains(@name,'code')]");
    protected By SEARCH_BUTTON = By.xpath("//input[contains(@src,'img/search.png')]");
    protected By ACTIVE_FIRST_PAGE_LINK = By.xpath("//span[contains(@class,'currentPage')][contains(text(),'1')]");
    protected By SECOND_PAGE_LINK = By.xpath("//a[contains(@class,'pagination')][contains(text(),'2')]");
    protected By THIRD_PAGE_LINK = By.xpath("//a[contains(@class,'pagination')][contains(text(),'3')]");
    protected By BODY = By.tagName("body");
    
    @Drone
    WebDriver driver;
    
    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }

    @Before
    public void openStartURL() throws MalformedURLException {
        // selenium.setSpeed(200);
        driver.navigate().to(new URL(contextPath.toString() + MAIN_PAGE));
    }

    @Test
    @InSequence(1)
    public void mainPageTest() {
        assertTrue("A page should contain text 'new'", isTextOnPage("new"));
        assertTrue("A page should contain text 'martin'", isTextOnPage("martin"));
        assertTrue("A page should contain text '6 Feb'", isTextOnPage("6 Feb"));
        assertTrue("A page should contain text 'Exposure'", isTextOnPage("Exposure"));
    }

    @Test
    @InSequence(2)
    public void newPublicPostTest() {
        driver.findElement(POST_AREA).clear();
        driver.findElement(POST_AREA).sendKeys(CODE_FRAGMENT);

        new Select(driver.findElement(SYNTAX_SELECT)).selectByVisibleText(JS_SYNTAX);

        driver.findElement(NAME_INPUT).clear();
        driver.findElement(NAME_INPUT).sendKeys("PublicTester");

        guardHttp(driver.findElement(SUBMIT_BUTTON)).click();

        assertTrue("Location is not correct", isLocationCorrect(driver.getCurrentUrl(), false));

        assertTrue("A page should contain text 'Posted by PublicTester just now'", isTextOnPage("Posted by PublicTester just now"));
        assertTrue("A page should contain pasted text", isTextOnPage(CODE_FRAGMENT_PART));
        assertTrue("A page should contain element 'DOWNLOAD'", element(DOWNLOAD_LINK).isPresent().apply(driver));
        
        assertTrue("It should be able to download file from database", isDownloadWorking(driver, DOWNLOAD_LINK, CODE_FRAGMENT_PART));

        assertTrue("A page should contain element 'Tester'", element(PUBLIC_TESTER_LINK).isPresent().apply(driver));
        assertTrue("A page should contain element 'JavaScript'", element(JS_TEXT).isPresent().apply(driver));
    }

    @Test
    @InSequence(3)
    public void newPrivatePostTest() {
        driver.findElement(POST_AREA).clear();
        driver.findElement(POST_AREA).sendKeys(CODE_FRAGMENT);

        new Select(driver.findElement(SYNTAX_SELECT)).selectByVisibleText(JS_SYNTAX);
        new Select(driver.findElement(EXPOSURE_SELECT)).selectByVisibleText(PRIVATE_EXPOSURE);

        driver.findElement(NAME_INPUT).clear();
        driver.findElement(NAME_INPUT).sendKeys("PrivateTester");

        guardHttp(driver.findElement(SUBMIT_BUTTON)).click();

        assertTrue("Location is not correct", isLocationCorrect(driver.getCurrentUrl(), true));
        assertTrue("A page should contain text 'Posted by PrivateTester just now'", isTextOnPage("Posted by PrivateTester just now"));
        assertTrue("A page should contain pasted text", isTextOnPage(CODE_FRAGMENT_PART));
        assertTrue("A page should contain element 'DOWNLOAD'", element(DOWNLOAD_LINK).isPresent().apply(driver));
        assertFalse("A page shouldn't contain element 'PrivateTester'", element(PRIVATE_TESTER_LINK).isPresent().apply(driver));
    }

    @Test
    @InSequence(4)
    public void recentPostsTest() {
        guardHttp(driver.findElement(CRAZYMAN_LINK)).click();
        assertTrue("A page should contain 'Posted by crazyman on 19 Feb'", isTextOnPage("Posted by crazyman on 19 Feb"));
        assertTrue("A page should contain code fragment", isTextOnPage("@GeneratedValue(strategy = GenerationType.IDENTITY)"));
    }

    @Test
    @InSequence(5)
    public void exactSearchTest() {
        guardHttp(driver.findElement(HISTORY_LINK)).click();
        assertTrue("A page should contain 'Posted by PublicTester'", isTextOnPage("Posted by PublicTester"));
        assertFalse("A page shouldn't contain 'Posted by PrivateTester'", isTextOnPage("Posted by PrivateTester"));

        driver.findElement(USER_SEARCH_INPUT).clear();
        driver.findElement(USER_SEARCH_INPUT).sendKeys("graham");

        driver.findElement(DATE_SEARCH_INPUT).clear();
        driver.findElement(DATE_SEARCH_INPUT).sendKeys("2009-02-02");

        new Select(driver.findElement(SYNTAX_SELECT)).selectByVisibleText(JS_SYNTAX);

        driver.findElement(CODE_SEARCH_INPUT).clear();
        driver.findElement(CODE_SEARCH_INPUT).sendKeys("toggle_visibility(id)");

        guardHttp(driver.findElement(SEARCH_BUTTON)).click();

        assertTrue("A page should contain 'Posted by graham ...'", isTextOnPage("Posted by graham on 3 Feb 2009"));
        assertTrue("A page should contain 'Language: JavaScript'", isTextOnPage("Language: JavaScript"));
        assertTrue("A page should contain the code fragment found", isTextOnPage("var e = document.ge"));
        assertFalse(element(ACTIVE_FIRST_PAGE_LINK).isPresent().apply(driver)); //assert that only one record was found
    }

    @Test
    @InSequence(6)
    public void searchAndPaginationTest() {
        guardHttp(driver.findElement(HISTORY_LINK)).click();
        driver.findElement(USER_SEARCH_INPUT).clear();
        driver.findElement(USER_SEARCH_INPUT).sendKeys("martin");
        new Select(driver.findElement(SYNTAX_SELECT)).selectByVisibleText(ANY_SYNTAX);

        driver.findElement(DATE_SEARCH_INPUT).clear();
        driver.findElement(CODE_SEARCH_INPUT).clear();

        guardHttp(driver.findElement(SEARCH_BUTTON)).click();
        assertTrue("A page should contain 'Posted by martin ...'", isTextOnPage("Posted by martin on 15 Feb 2009"));
        guardHttp(driver.findElement(SECOND_PAGE_LINK)).click();
        assertTrue("A page should contain 'Posted by martin ...'", isTextOnPage("Posted by martin on 1 Feb 2009"));
        guardHttp(driver.findElement(THIRD_PAGE_LINK)).click();
        assertTrue("A page should contain 'Posted by martin ...'", isTextOnPage("Posted by martin on 9 Jan 2009"));
        assertTrue("A page should contain code fragment", isTextOnPage("function build_calendar($month,$year,$dateArray)"));
    }

    @Test
    @InSequence(7)
    public void helpPageTest() {
        guardHttp(driver.findElement(HELP_LINK)).click();
        assertTrue("A page should contain help information", isTextOnPage("Useful Information"));
        assertTrue("A page should contain 'Weld Features Covered'", isTextOnPage("Weld Features Covered"));
    }

    private boolean isLocationCorrect(String urlStr, boolean privatePost) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException ex) {
            fail("Invalid location, can't create URL: " + urlStr);
        }
        
        String regexp;

        if (!privatePost) {
            regexp = "[0-9]{1,}";
        } else {
            regexp = "[a-z0-9]{6}";
        }

        return url.getPath().substring("/weld-pastecode/".length()).matches(regexp);
    }

    private boolean isDownloadWorking(WebDriver driver, By by, String textToFind) {
        try {
            URL downloadUrl = new URL(driver.findElement(by).getAttribute("href"));
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
            "function addleadingzeros(array) {" + "\n"
            + "var highestint = Math.max.apply(Math,array);" + "\n"
            + "for(var i=0; i<array.length; i++) {" + "\n"
            + "var nleadingzeros = highestint.toString().length - array[i].toString().length;" + "\n"
            + "for(var j=0; j<nleadingzeros; j++) array[i] = '0' + array[i];" + "\n"
            + "}" + "\n"
            + "return array;" + "\n"
            + "}";
    protected String CODE_FRAGMENT_PART = CODE_FRAGMENT.substring(0, 30);

    private boolean isTextOnPage(String text) {
        return element(BODY).textContains(text).apply(driver);
    }
}
